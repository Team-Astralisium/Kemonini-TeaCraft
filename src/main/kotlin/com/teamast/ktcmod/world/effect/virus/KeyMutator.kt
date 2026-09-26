package com.teamast.ktcmod.world.effect.virus

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.KTCModConfig
import com.teamast.ktcmod.world.effect.EffectRegistries
import com.teamast.ktcmod.world.effect.ModMobEffect
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.EntityTypeTags
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.CommandEvent
import net.neoforged.neoforge.event.entity.living.EffectParticleModificationEvent
import net.neoforged.neoforge.event.entity.living.MobEffectEvent

/**
 * 设定（默认总时长 45 游戏日，可由配置 `key_mutator.time_scale_steps` 整体缩放，见 [totalDurationTicks]）：
 * 1. **对亡灵生物无效**：`minecraft:undead` 实体类型 tag（可被数据包扩展）在
 *    [MobEffectEvent.Applicable] 阶段就被拒绝；若通过存档等途径硬塞进来，
 *    [applyEffectTick] 会返回 false 让原版移除（因为刷新周期是 1 游戏日，最迟 1 游戏日内生效）。
 * 2. **效果粒子禁用**（HUD 图标保留）：见 [onEffectParticleModification]。
 * 3. **5 个病程阶段**见 [KeyMutatorStage]（默认 15 / 10 / 5 / 10 / 5 天），供客户端按阶段换模型。
 * 4. **掉血窗口内线性削减最大生命值**（默认流程下即前 40 天），窗口结束时固定为「获得效果时」的
 *    一半：用一个数值随进度变化的 transient modifier 实现，每 [HEALTH_UPDATE_INTERVAL_TICKS]
 *    （即 1 游戏日）刷新一次；基准值（获得效果时的最大生命值）记在实体的 persistentData 里。
 * 5. **无法被正常手段移除**（牛奶、其他模组的净化等）：[MobEffectEvent.Remove] 直接取消移除；
 *    只有 `/effect clear` 之类的**指令**（由 [onCommand] 标记所在 tick）与实体死亡会放行，
 *    且指令移除时**不判定死亡**。
 * 6. **自然到期**时：带 `cure_consumed` / `beautifully_death` 任一 tag 的实体活下来并获得残余效果，
 *    其余实体因器官衰竭死亡（见 [OrganFailure]）。
 */
@EventBusSubscriber(modid = KTCMod.MODID)
class KeyMutator : ModMobEffect(MobEffectCategory.HARMFUL, -26215) {
    init {
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            Identifier.fromNamespaceAndPath(KTCMod.MODID, "effect.key_mutator_0"),
            -0.008,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        )
        this.addAttributeModifier(
            Attributes.ATTACK_DAMAGE,
            Identifier.fromNamespaceAndPath(KTCMod.MODID, "effect.key_mutator_1"),
            -0.05,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        )
        this.addAttributeModifier(
            Attributes.ATTACK_SPEED,
            Identifier.fromNamespaceAndPath(KTCMod.MODID, "effect.key_mutator_2"),
            -0.02,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        )
    }

    // 服务端逐 tick 逻辑
    override fun shouldApplyEffectTickThisTick(tickCount: Int, amplifier: Int): Boolean =
        tickCount % HEALTH_UPDATE_INTERVAL_TICKS == 0
    override fun applyEffectTick(level: ServerLevel, entity: LivingEntity, amplifier: Int): Boolean {
        //亡灵生物本不该拿到这个效果，万一被硬塞进来就让原版移除它
        if (entity.`is`(EntityTypeTags.UNDEAD)) {
            return false
        }
        val instance = entity.getEffect(EffectRegistries.KEY_MUTATOR)
        refreshMaxHealthPenalty(entity, if (instance == null) 0 else KeyMutatorStage.elapsedTicks(instance.duration))
        return true
    }

    override fun onEffectAdded(entity: LivingEntity, amplifier: Int) {
        super.onEffectAdded(entity, amplifier)
        if (entity.level().isClientSide) {
            return
        }
        // 记录基准值并立刻套用一次，免得要等一个刷新周期
        originalMaxHealth(entity)
        refreshMaxHealthPenalty(entity, 0)
    }

    override fun removeAttributeModifiers(attributes: AttributeMap) {
        super.removeAttributeModifiers(attributes)
        attributes.getInstance(Attributes.MAX_HEALTH)?.removeModifier(MAX_HEALTH_MODIFIER_ID)
    }

    companion object {
        /** 1 游戏日 = 24000 tick
         * 默认总流程：3 档 = 45 游戏日
         * 配置滑条的步长：1 档 = 15 游戏日（5 个阶段的最小单位是 5 天，15 天正好整除）
         * */
        const val TICKS_PER_DAY: Int = 24_000
        const val DAYS_PER_STEP: Int = 15
        const val DEFAULT_TOTAL_DAYS: Int = 3 * DAYS_PER_STEP
        //默认档位
        const val DEFAULT_STEPS: Int = DEFAULT_TOTAL_DAYS / DAYS_PER_STEP
        //最小档位：1 档 = 15 游戏日
        const val MIN_STEPS: Int = 1
        //上限3650，实际3645
        const val MAX_TOTAL_DAYS: Int = 3650
        //滑条最大档位
        const val MAX_STEPS: Int = MAX_TOTAL_DAYS / DAYS_PER_STEP
        //掉血窗口占整个流程的比例（默认流程下即前 40 / 45 天）
        const val HEALTH_PENALTY_FLOW_FRACTION: Double = 40.0 / 45.0
        /**
         * 当前配置下的效果总时长（tick）。
         * `key_mutator.time_scale_steps` 的单位是「档」（1 档 = [DAYS_PER_STEP] 天），
         * 5 个阶段与掉血进度都按这个总长整体缩放。
         */
        @JvmStatic
        fun totalDurationTicks(): Int =
            KTCModConfig.KEY_MUTATOR_TIME_SCALE_STEPS.get() * DAYS_PER_STEP * TICKS_PER_DAY

        //总流程天数
        @JvmStatic
        fun totalDays(): Int = KTCModConfig.KEY_MUTATOR_TIME_SCALE_STEPS.get() * DAYS_PER_STEP

        //第 40 天结束时最大生命值 = 原来的 50%
        const val HEALTH_PENALTY_FRACTION: Double = 0.5

        /** 最大生命值的刷新间隔：1 游戏日一次。
         * 注意 `shouldApplyEffectTickThisTick` 收到的是**剩余时长**，
         * 总时长是24000 的整数倍，所以取模正好落在每个游戏日的边界上（默认流程共 45 次判定）。
         * */
        const val HEALTH_UPDATE_INTERVAL_TICKS: Int = TICKS_PER_DAY

        //残余效果（效果结束时给活下来的实体）持续时间
        private const val RESIDUAL_DURATION_TICKS = 200

        //削减最大生命值用的 modifier：transient（不入存档），由 tick 重建。
        private val MAX_HEALTH_MODIFIER_ID: Identifier =
            Identifier.fromNamespaceAndPath(KTCMod.MODID, "effect.key_mutator.max_health")

        //记录「获得效果时」最大生命值的 persistentData 键
        private const val ORIGINAL_MAX_HEALTH_KEY: String = "${KTCMod.MODID}_key_mutator_original_max_health"

        // 效果自然结束时，拥有其中任一 tag 的实体不会死亡。
        private val SURVIVAL_TAGS: Set<String> = setOf("cure_consumed", "beautifully_death")

        private val WHITESPACE: Regex = Regex("\\s+")

        //记录 `/effect clear` 之类指令所在的 server tick；只有该 tick 内的移除会被放行。
        private var commandRemovalTick: Long = Long.MIN_VALUE

        /** 最大生命值
         * 取得（并在首次访问时记录）「获得效果时」的最大生命值。
         */
        private fun originalMaxHealth(entity: LivingEntity): Double {
            val data = entity.persistentData
            if (!data.contains(ORIGINAL_MAX_HEALTH_KEY)) {
                data.putDouble(ORIGINAL_MAX_HEALTH_KEY, entity.getAttributeValue(Attributes.MAX_HEALTH))
            }
            return data.getDoubleOr(ORIGINAL_MAX_HEALTH_KEY, entity.getAttributeValue(Attributes.MAX_HEALTH))
        }

        /**
         * 把最大生命值的削减量刷新到 [elapsedTicks] 对应的位置：前 40 天从 0 线性增长到
         * `-原始最大生命值 × 50%`，之后保持不变。
         */
        private fun refreshMaxHealthPenalty(entity: LivingEntity, elapsedTicks: Int) {
            val attribute = entity.getAttribute(Attributes.MAX_HEALTH) ?: return
            val amount = -originalMaxHealth(entity) * HEALTH_PENALTY_FRACTION *
                KeyMutatorStage.healthPenaltyProgress(elapsedTicks)
            attribute.addOrUpdateTransientModifier(
                AttributeModifier(MAX_HEALTH_MODIFIER_ID, amount, AttributeModifier.Operation.ADD_VALUE)
            )
        }

        /** 清理 modifier 与记录的基准值（效果真正结束或实体死亡时调用）。 */
        private fun clearStoredState(entity: LivingEntity) {
            entity.persistentData.remove(ORIGINAL_MAX_HEALTH_KEY)
            entity.getAttribute(Attributes.MAX_HEALTH)?.removeModifier(MAX_HEALTH_MODIFIER_ID)
        }

        //事件

        /**
         * 2. 不刷效果粒子。
         *
         * 原版在 `LivingEntity#updateSynchronizedMobEffectParticles` 里按
         * `MobEffectInstance#isVisible` 筛选要刷哪些效果的粒子，NeoForge 把这段换成了
         * [EffectParticleModificationEvent]；在这里把本效果标记为不可见即可，
         * 于是**无论效果是携带体、指令还是存档数据挂上的都不会冒粒子**。
         * HUD 上的效果图标由 `MobEffectInstance#showIcon` 控制，不受影响。
         */
        @JvmStatic
        @SubscribeEvent
        fun onEffectParticleModification(event: EffectParticleModificationEvent) {
            if (event.effect.effect.value() !== EffectRegistries.KEY_MUTATOR.get()) {
                return
            }
            event.isVisible = false
        }

        /** 1. 亡灵生物无法获得此效果。 */
        @JvmStatic
        @SubscribeEvent
        fun onApplicable(event: MobEffectEvent.Applicable) {
            if (event.effectInstance.effect.value() !== EffectRegistries.KEY_MUTATOR.get()) {
                return
            }
            if (event.entity.`is`(EntityTypeTags.UNDEAD)) {
                event.result = MobEffectEvent.Applicable.Result.DO_NOT_APPLY
            }
        }

        /**
         * 5. 正常手段无法移除：取消移除事件。
         *
         * 只有两种情况放行：
         * - 指令移除（[onCommand] 标记了当前 tick）——按约定不判定死亡；
         * - 实体正在死亡，让原版正常清理。
         */
        @JvmStatic
        @SubscribeEvent
        fun onRemove(event: MobEffectEvent.Remove) {
            if (event.effect.value() !== EffectRegistries.KEY_MUTATOR.get()) {
                return
            }
            val entity = event.entity
            if (entity.isDeadOrDying || isCommandRemoval(entity)) {
                clearStoredState(entity)
                return
            }
            event.isCanceled = true
        }

        /** 识别 `/effect clear ...`（含 `/execute ... run effect clear ...`），只标记当前 tick。 */
        @JvmStatic
        @SubscribeEvent
        fun onCommand(event: CommandEvent) {
            val tokens = event.parseResults.reader.string.trim().removePrefix("/").split(WHITESPACE)
            for (index in 0 until tokens.size - 1) {
                if (tokens[index].substringAfter(':') == "effect" && tokens[index + 1].substringAfter(':') == "clear") {
                    commandRemovalTick = event.parseResults.context.source.server.tickCount.toLong()
                    return
                }
            }
        }

        /** 6. 自然到期：带 tag 的活下来（并获得残余效果），其余因器官衰竭死亡。 */
        @JvmStatic
        @SubscribeEvent
        fun onKeyMutatorExpired(event: MobEffectEvent.Expired) {
            val expiredEffect = event.effectInstance ?: return
            if (expiredEffect.effect.value() !== EffectRegistries.KEY_MUTATOR.get()) {
                return
            }

            val entity = event.entity
            clearStoredState(entity)
            if (!entity.isAlive) {
                return
            }

            // 亡灵生物本就不该有这个效果：[applyEffectTick] 返回 false 让原版移除它时，
            // 也会走到这个事件，这里必须直接放行，否则会误杀亡灵。
            if (entity.`is`(EntityTypeTags.UNDEAD)) {
                return
            }

            if (SURVIVAL_TAGS.none { entity.entityTags().contains(it) }) {
                OrganFailure.kill(entity)
                return
            }

            entity.addEffect(
                MobEffectInstance(
                    EffectRegistries.RESIDUAL,
                    RESIDUAL_DURATION_TICKS,
                    expiredEffect.amplifier,
                )
            )
        }

        /** 当前 tick 是否处于「指令移除」窗口内。 */
        private fun isCommandRemoval(entity: LivingEntity): Boolean {
            val level = entity.level() as? ServerLevel ?: return false
            return level.server.tickCount.toLong() == commandRemovalTick
        }
    }
}
