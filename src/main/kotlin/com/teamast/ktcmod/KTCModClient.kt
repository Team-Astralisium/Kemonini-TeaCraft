package com.teamast.ktcmod

import com.teamast.ktcmod.world.effect.EffectRegistries
import com.teamast.ktcmod.world.entity.EntityRegistries
import com.teamast.ktcmod.client.renderer.PeddlerRenderer
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.effect.MobEffectInstance
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod(value = KTCMod.MODID, dist = [Dist.CLIENT]) @EventBusSubscriber(modid = KTCMod.MODID, value = [Dist.CLIENT])
class KTCModClient(container: ModContainer?) {
    init {
        container?.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { mod: ModContainer?,
                                   parent: Screen? -> mod?.let { parent?.let { it1 -> ConfigurationScreen(it, it1) } }!! })
    }

    companion object {
        @JvmStatic @SubscribeEvent fun onClientSetup(event: FMLClientSetupEvent?) {
            KTCMod.LOGGER.info("Setting up client configuration for " + KTCMod.MODID + "...")
        }

        @JvmStatic @SubscribeEvent fun registerRenderers(event: EntityRenderersEvent.RegisterRenderers) {
            event.registerEntityRenderer(EntityRegistries.PEDDLER.get(), ::PeddlerRenderer)
        }

        @JvmStatic
        @SubscribeEvent
        fun registerMobEffectExtensions(event: RegisterClientExtensionsEvent) {
            event.registerMobEffect(object : IClientMobEffectExtensions {
                override fun isVisibleInInventory(effect: MobEffectInstance): Boolean {
                    return false
                }

                override fun renderInventoryText(
                    instance: MobEffectInstance,
                    screen: AbstractContainerScreen<*>,
                    guiGraphics: GuiGraphicsExtractor,
                    x: Int,
                    y: Int,
                    blitOffset: Int
                ): Boolean {
                    return false
                }

                override fun isVisibleInGui(effect: MobEffectInstance): Boolean {
                    return true
                }
            }, EffectRegistries.KEY_MUTATOR.get())
        }
    }
}
