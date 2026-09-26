package com.teamast.ktcmod.world.item.misc

import com.teamast.ktcmod.world.effect.EffectRegistries
import com.teamast.ktcmod.world.effect.virus.KeyMutator
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * 病毒携带体。
 *
 * 右键使用会套用【食用】动作，食用完毕后：
 * 1. 若使用者身上还没有 [EffectRegistries.KEY_MUTATOR]，挂上 I 级效果，时长取配置
 *    `key_mutator.time_scale_steps`（默认 45 游戏日，见 [KeyMutator.totalDurationTicks]）；
 *    已经带着该效果时**不产生任何效果**——原病程继续走，不会被刷新；
 * 2. 本体被消耗，并转化为空的携带体（由 `ItemRegistries.CARRIER` 上的
 *    `use_remainder` 组件决定）。
 *
 * 食用动作的具体表现（动作类型、音效、粒子）由物品注册时的 `CONSUMABLE` 组件决定，
 * 详见 [com.teamast.ktcmod.world.item.ItemRegistries.CARRIER]；
 * 其中「后段抖动」由客户端 Mixin 去除，见 `com.teamast.ktcmod.mixin.client.ItemInHandRendererMixin`。
 *
 * 效果本身不产生粒子，由 [KeyMutator] 统一处理（见 `KeyMutator.onEffectParticleModification`），
 * 所以这里按原版默认参数挂效果即可。
 */
class ItemCarrier(properties: Properties) : Item(properties) {

    override fun finishUsingItem(stack: ItemStack, level: Level, entity: LivingEntity): ItemStack {
        // 交给 CONSUMABLE 组件处理消耗、音效、统计与剩余物转换
        val result = super.finishUsingItem(stack, level, entity)

        // 只在服务端真正施加效果；已经在病程中的实体再吃一个不产生任何效果（不刷新时长）
        if (!level.isClientSide && !entity.hasEffect(EffectRegistries.KEY_MUTATOR)) {
            entity.addEffect(
                MobEffectInstance(
                    EffectRegistries.KEY_MUTATOR,
                    KeyMutator.totalDurationTicks(),
                    KEY_MUTATOR_AMPLIFIER,
                )
            )
        }
        return result
    }

    //一些数值设定
    companion object {
        /** I 级效果对应 amplifier 0。 */
        const val KEY_MUTATOR_AMPLIFIER: Int = 0
    }
}
