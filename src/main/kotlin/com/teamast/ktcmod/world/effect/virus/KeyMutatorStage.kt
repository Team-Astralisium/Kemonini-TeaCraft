package com.teamast.ktcmod.world.effect.virus

import com.teamast.ktcmod.world.effect.EffectRegistries
import net.minecraft.world.entity.LivingEntity

/**
 * KeyMutator 病程的 5 个阶段。
 *
 * 阶段按**比例**划分（默认流程下即 [startDay]–[endDay] 天）：
 * 潜伏期 0–15 → 第一阶段 15–25 → 第二阶段 25–30 → 第三阶段 30–40 → 第四阶段 40–45。
 * 总流程天数由配置 `key_mutator.time_scale` 决定（15 ~ 3650 天，默认 45 天），
 * 5 个阶段与掉血进度都会随之整体缩放，所以这里保存的是「基准 45 天下的天数」，
 * 实际边界用比例换算（整数交叉相乘，避免浮点误差）。
 *
 * 客户端据此更换效果获得者的模型：渲染状态里会写入本枚举（见
 * `com.teamast.ktcmod.client.render.KeyMutatorClientRenderer`），
 * 具体的 5 套模型由 `com.teamast.ktcmod.client.render.KeyMutatorPlaceholderRenderer` 负责（尚未实现）。
 *
 * 注意：阶段以「从获得效果起的绝对 tick 数」计算，与效果实例的剩余时长一起反推；
 * 用 `/effect give` 给了更短的时长时就只会走到对应阶段。
 */
enum class KeyMutatorStage(
    val startDay: Int,
    val endDay: Int,
) {
    INCUBATION(0, 15),
    STAGE_1(15, 25),
    STAGE_2(25, 30),
    STAGE_3(30, 40),
    STAGE_4(40, 45);

    companion object {
        /** 当前配置下该阶段开始的 tick 数。 */
        fun startTicksOf(stage: KeyMutatorStage): Int =
            (stage.startDay.toLong() * KeyMutator.totalDurationTicks() / KeyMutator.DEFAULT_TOTAL_DAYS).toInt()

        /** 当前配置下该阶段结束的 tick 数。 */
        fun endTicksOf(stage: KeyMutatorStage): Int =
            (stage.endDay.toLong() * KeyMutator.totalDurationTicks() / KeyMutator.DEFAULT_TOTAL_DAYS).toInt()

        /** 由「剩余时长」反推已经过的 tick 数（并夹在 0..总时长 之间）。 */
        fun elapsedTicks(remainingTicks: Int): Int {
            val total = KeyMutator.totalDurationTicks()
            return (total - remainingTicks).coerceIn(0, total)
        }

        /** 已经过 [elapsedTicks] tick 时处于哪个阶段。 */
        fun at(elapsedTicks: Int): KeyMutatorStage {
            val total = KeyMutator.totalDurationTicks().toLong()
            return entries.last {
                elapsedTicks.toLong() * KeyMutator.DEFAULT_TOTAL_DAYS >= it.startDay.toLong() * total
            }
        }

        /** 实体身上的 KeyMutator 已经过了多久；没有该效果时返回 null。 */
        fun elapsedTicks(entity: LivingEntity): Int? =
            entity.getEffect(EffectRegistries.KEY_MUTATOR)?.let { elapsedTicks(it.duration) }

        /** 实体当前所处阶段；没有该效果时返回 null。 */
        fun of(entity: LivingEntity): KeyMutatorStage? = elapsedTicks(entity)?.let(::at)

        /**
         * 最大生命值削减进度：0（刚获得效果）→ 1（掉血窗口结束，默认流程即第 40 天）。
         * 之后保持 1。
         */
        fun healthPenaltyProgress(elapsedTicks: Int): Double {
            val window = KeyMutator.totalDurationTicks() * KeyMutator.HEALTH_PENALTY_FLOW_FRACTION
            return (elapsedTicks.toDouble() / window).coerceIn(0.0, 1.0)
        }
    }
}
