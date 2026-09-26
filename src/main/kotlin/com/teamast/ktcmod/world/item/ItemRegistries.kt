package com.teamast.ktcmod.world.item

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.sounds.SoundEvents as KTCSoundEvents
import com.teamast.ktcmod.world.item.misc.ItemCarrier
import com.teamast.ktcmod.world.item.misc.ItemOST
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemUseAnimation
import net.minecraft.world.item.JukeboxSong
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.Consumable
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ItemRegistries {
    @JvmField
    val ITEMS = DeferredRegister.createItems(KTCMod.MODID)

    // 这个枚举类是注册物品的一些数据的集合
    enum class KTCModItems(val registryName: String) {
        BISCUIT("biscuit"),
        TOOLKIT_1("beginner_toolkit"),
        TOOLKIT_2("intermidiate_toolkit"),
        TOOLKIT_3("advanced_toolkit"),
        TOOLKIT_4("superior_toolkit"),
        TABLET("vitamin_tablet"),
        COIN("deprecated_coin"),
    }

    @JvmField
    val OST_BOX = ITEMS.registerItem("ost_box", ::ItemOST) { properties ->
        properties
            .stacksTo(DEFAULT_SPECIFIC_STACKSIZE)
            .rarity(Rarity.EPIC)
    }

    // 空的携带体：病毒携带体被食用后留下的空壳
    // 必须声明在 CARRIER 之前：CARRIER 的属性里要用到它，而 DeferredRegister 按声明顺序注册
    @JvmField
    val EMPTY_CARRIER = ITEMS.registerSimpleItem("empty_carrier") { properties ->
        properties
            .stacksTo(SPECIAL_STACKSIZE)
            .rarity(Rarity.COMMON)
    }

    // 病毒携带体：食用后给使用者挂上 I 级 KeyMutator（45 游戏日），并转化为空的携带体
    @JvmField
    val CARRIER = ITEMS.registerItem("carrier", ::ItemCarrier) { properties ->
        properties
            .stacksTo(SPECIAL_STACKSIZE)
            .rarity(Rarity.RARE)
            // 食用动作：EAT 动作 + 专属食用音效（当前是静音占位，见 sounds/SoundEvents.kt）；关闭碎屑粒子
            .component(
                DataComponents.CONSUMABLE,
                Consumable.builder()
                    .animation(ItemUseAnimation.EAT)
                    .sound(KTCSoundEvents.CARRIER_USE)
                    .hasConsumeParticles(false)
                    .build()
            )
            // 食用完毕后本体转化为空的携带体
            .usingConvertsTo(EMPTY_CARRIER.get())
    }

    @JvmField
    val MISC_ITEMS: Map<KTCModItems, DeferredItem<Item>> = KTCModItems.entries.associateWith { item ->
        ITEMS.registerSimpleItem(item.registryName) { properties ->
            properties
                .stacksTo(DEFAULT_MAX_STACKSIZE)
                .rarity(Rarity.COMMON)
        }
    }

    @JvmField
    val DISCS: MutableList<DeferredItem<Item>> = mutableListOf()
    init {
        for (i in 1..21) {
            val discName = discItemName(i)
            val itemHolder = ITEMS.registerSimpleItem(discName) { properties ->
                properties
                    .stacksTo(MODIFIED_STACKSIZE)
                    .rarity(Rarity.RARE)
                    .jukeboxPlayable(discSongKey(i))
            }
            DISCS += itemHolder
        }
    }

    fun getDisc(index: Int): DeferredItem<Item> = DISCS[index - 1]

    fun getMiscItem(item: KTCModItems): DeferredItem<Item> = MISC_ITEMS.getValue(item)

    fun register(eventBus: IEventBus) {
        ITEMS.register(eventBus)
    }

    private fun discItemName(index: Int): String = "ost_disc_$index"

    private fun discSongKey(index: Int): ResourceKey<JukeboxSong> {
        return ResourceKey.create(
            Registries.JUKEBOX_SONG,
            Identifier.fromNamespaceAndPath(KTCMod.MODID, discItemName(index))
        )
    }

    private const val DEFAULT_MAX_STACKSIZE = 64
    private const val DEFAULT_SPECIFIC_STACKSIZE = 16
    private const val MODIFIED_STACKSIZE = 4
    private const val SPECIAL_STACKSIZE = 1

}
