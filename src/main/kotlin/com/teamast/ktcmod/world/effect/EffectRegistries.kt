package com.teamast.ktcmod.world.effect

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.world.effect.virus.KeyMutator
import net.minecraft.core.registries.Registries
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister


object EffectRegistries {
    @JvmField
    val EFFECTS_REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, KTCMod.MODID)

    @JvmField
    val KEY_MUTATOR = EFFECTS_REGISTER.register("key_mutator", ::KeyMutator)

    @JvmField
    val RESIDUAL = EFFECTS_REGISTER.register("residual", ::ResidualEffect)

    fun register(eventBus: IEventBus) {
        EFFECTS_REGISTER.register(eventBus)
    }
}
