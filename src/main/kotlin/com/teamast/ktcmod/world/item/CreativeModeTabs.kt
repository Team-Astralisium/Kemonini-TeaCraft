package com.teamast.ktcmod.world.item

import com.teamast.ktcmod.KTCMod
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object CreativeModeTabs {
    // 注册创造模式物品栏
    @JvmStatic
    val CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KTCMod.MODID)
    // 杂项
    val KTCMOD_CREATIVE_TAB_1 = CREATIVE_MODE_TABS.register("ktcmod_tab_1", Supplier {
        CreativeModeTab
            .builder()
            .title(Component.translatable("itemGroup.kemono_teatime.misc"))
            .icon { ItemRegistries.OST_BOX.toStack() }
            .displayItems { _, output ->
                output.accept(ItemRegistries.OST_BOX.get())
                output.accept(ItemRegistries.CARRIER.get())
                output.accept(ItemRegistries.EMPTY_CARRIER.get())
                ItemRegistries.MISC_ITEMS.values.forEach { output.accept(it.get()) }
                ItemRegistries.DISCS.forEach { output.accept(it.get()) }
            }
            .build()
    })
    // 方块
    val KTCMOD_CREATIVE_TAB_2 = CREATIVE_MODE_TABS.register("ktcmod_tab_2", Supplier {
        CreativeModeTab
            .builder()
            .title(Component.translatable("itemGroup.kemono_teatime.blocks"))
            .icon { ItemRegistries.OST_BOX.toStack() }
            .withTabsBefore(KTCMOD_CREATIVE_TAB_1.id)
            .build()
    })

    fun register(eventBus: IEventBus) {
        CREATIVE_MODE_TABS.register(eventBus)
        KTCMod.LOGGER.info("Generating Creative Mode Tabs for " + KTCMod.MODID + "...")
    }
}
