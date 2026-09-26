package com.teamast.ktcmod

import com.teamast.ktcmod.world.effect.virus.KeyMutator
import net.neoforged.neoforge.common.ModConfigSpec

object KTCModConfig {
    private val BUILDER = ModConfigSpec.Builder()
    @JvmField val ENABLE_PITY: ModConfigSpec.BooleanValue
    @JvmField val PITY_THRESHOLD: ModConfigSpec.IntValue
    @JvmField val REPLACE_WANDERING_TRADER_WITH_PEDDLER: ModConfigSpec.BooleanValue
    @JvmField val CARRIER_CHEST_CHANCE: ModConfigSpec.DoubleValue
    @JvmField val KEY_MUTATOR_TIME_SCALE_STEPS: ModConfigSpec.IntValue

    init {
        BUILDER.push("disc_box")
        ENABLE_PITY = BUILDER
            .define("pity_enabled", true)
        PITY_THRESHOLD = BUILDER
            .defineInRange("pity_threshold", 16, 1, 1000)
        BUILDER.pop()

        BUILDER.push("world_gen")
        REPLACE_WANDERING_TRADER_WITH_PEDDLER = BUILDER
            .define("replace_wandering_trader_with_peddler", true)
        // 每个箱子战利品表生成病毒携带体的概率，0.002 即 2‰；设为 0 可关闭
        CARRIER_CHEST_CHANCE = BUILDER
            .defineInRange("carrier_chest_chance", 0.002, 0.0, 1.0)
        BUILDER.pop()

        BUILDER.push("key_mutator")
        // 病程总时长（滑条，1 档 = 15 游戏日）：5 个阶段与前 40/45 的掉血进度一起整体缩放
        // 注意键名带 _steps：单位是「档」而不是「天」，避免旧配置（按天填写）被静默重新解释
        KEY_MUTATOR_TIME_SCALE_STEPS = BUILDER
            .comment("Time scale in steps of 15 days (default 3 steps = 45 days).")
            .defineInRange(
                "time_scale_steps",
                KeyMutator.DEFAULT_STEPS,
                KeyMutator.MIN_STEPS,
                KeyMutator.MAX_STEPS,
            )
        BUILDER.pop()
    }

    val SPEC: ModConfigSpec = BUILDER.build()
}
