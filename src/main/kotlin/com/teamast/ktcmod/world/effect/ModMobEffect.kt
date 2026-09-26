package com.teamast.ktcmod.world.effect

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory

/**
 * 模组内所有自建状态效果的统一基类。
 *
 * 需要新增效果时继承此类，而不是直接继承原版的 [MobEffect]。
 */
open class ModMobEffect(
    category: MobEffectCategory,
    color: Int,
) : MobEffect(category, color)
