package com.teamast.ktcmod.world.effect

import net.minecraft.world.effect.MobEffectCategory

/**
 * 占位 DEBUFF：KeyMutator 到期后转变的目标。
 *
 * 后续设计确定后再补上具体效果逻辑。
 */
class ResidualEffect : ModMobEffect(MobEffectCategory.HARMFUL, 0xFFFF55)
