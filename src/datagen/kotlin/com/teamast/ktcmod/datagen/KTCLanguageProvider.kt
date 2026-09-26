package com.teamast.ktcmod.datagen

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.world.item.ItemRegistries
import com.google.gson.JsonObject
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.resources.Identifier
import java.util.concurrent.CompletableFuture

// 语言文件数据生成
class KTCLanguageProvider(
    output: PackOutput,
    private val locale: String,
    private val entriesFactory: () -> Map<String, String>
) : DataProvider {

    private val pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang")
    private val localeId = Identifier.fromNamespaceAndPath(KTCMod.MODID, locale)

    override fun run(cache: CachedOutput): CompletableFuture<*> {
        val root = JsonObject()
        entriesFactory().forEach { (key, value) -> root.addProperty(key, value) }
        return DataProvider.saveStable(cache, root, pathProvider.json(localeId))
    }

    override fun getName(): String = "Languages ($localeId)"

    companion object {
        /**
         * 英文翻译
         */
        fun englishTranslations(): Map<String, String> {
            val translations = linkedMapOf(
                "entity.${KTCMod.MODID}.peddler" to "Suichi",
                "item.${KTCMod.MODID}.ost_box" to "Kemono Teatime OST",
                "item.${KTCMod.MODID}.carrier" to "Carrier",
                "item.${KTCMod.MODID}.empty_carrier" to "Empty Carrier",
                "death.attack.kemono_teatime.organ_failure" to "%1\$s died of organ failure",
                "advancements.${KTCMod.MODID}.final_disc.title" to "Final Disc",
                "advancements.${KTCMod.MODID}.final_disc.description" to "Obtain ost_disc_21",
                "itemGroup.${KTCMod.MODID}.blocks" to "Kemonini TeaCraft: Blocks",
                "itemGroup.${KTCMod.MODID}.misc" to "Kemonini TeaCraft: Miscs",
                "config.${KTCMod.MODID}.pity_state.desc" to "Enable pity system for disc box.",
                "config.${KTCMod.MODID}.pity_threshold.desc" to "Number of uses without disc21 before guarantee triggers",
                "config.${KTCMod.MODID}.replace_trader_with_peddler" to "Replace Wandering Trader with Suichi.\nWhen enabled, Wandering Traders will be replaced by Suichi and their llamas will be removed.",
                "${KTCMod.MODID}.$conf.world_gen.carrier_chest_chance" to "Carrier Chest Chance",
                "${KTCMod.MODID}.$conf.world_gen.carrier_chest_chance.tooltip" to "Chance for each chest loot table to contain a Carrier. 0.002 = 2 permille, 0 disables it.",
                "${KTCMod.MODID}.$conf.world_gen" to "World Generations",
                "${KTCMod.MODID}.$conf.disc_box" to "Disc Box",
                "${KTCMod.MODID}.$conf.key_mutator" to "KeyMutator Gameplay",
                "${KTCMod.MODID}.$conf.key_mutator.time_scale" to "KeyMutator Gameplay - Time Scale",
                "${KTCMod.MODID}.$conf.key_mutator.time_scale.tooltip" to "Default flow is 45 days (1 step = 15 days).",
            )

            for (miscItem in ItemRegistries.KTCModItems.entries) {
                translations["item.${KTCMod.MODID}.${miscItem.registryName}"] = englishMiscName(miscItem)
            }

            for (disc in DiscTrack.entries) {
                translations["item.${KTCMod.MODID}.${disc.itemName}"] = disc.displayName
                translations["item.${KTCMod.MODID}.${disc.itemName}.desc"] = "Melodious Moments: Kemono Teatime OST - ${disc.displayName}"
            }
            return translations
        }

        /**
         * 中文翻译
         */
        fun chineseTranslations(): Map<String, String> {
            val translations = linkedMapOf(
                "entity.${KTCMod.MODID}.peddler" to "什锦",
                "item.${KTCMod.MODID}.ost_box" to "《兽娘红茶馆》原声带集",
                "item.${KTCMod.MODID}.carrier" to "携带体",
                "item.${KTCMod.MODID}.empty_carrier" to "空的携带体",
                "death.attack.kemono_teatime.organ_failure" to "%1\$s因器官衰竭而死",
                "advancements.${KTCMod.MODID}.final_disc.title" to "终曲唱片",
                "advancements.${KTCMod.MODID}.final_disc.description" to "获得 ost_disc_21",
                "itemGroup.${KTCMod.MODID}.blocks" to "兽娘红茶馆：方块",
                "itemGroup.${KTCMod.MODID}.misc" to "兽娘红茶馆：杂项",

                "${KTCMod.MODID}.$conf.world_gen.carrier_chest_chance" to "携带体箱子生成概率",
                "${KTCMod.MODID}.$conf.world_gen.carrier_chest_chance.tooltip" to "每张箱子战利品表生成病毒携带体的概率。0.002 即 2‰，设为 0 可关闭。",
                "${KTCMod.MODID}.$conf.pity_state.desc" to "是否启用保底机制",
                "${KTCMod.MODID}.$conf.pity_threshold.desc" to "触发保底的使用次数阈值",
                "${KTCMod.MODID}.$conf.replace_trader_with_peddler" to "用什锦替换流浪商人。\n启用后，流浪商人将被什锦替换，并移除伴随的羊驼。",
                "${KTCMod.MODID}.$conf.pity_enabled" to "启用保底机制",
                "${KTCMod.MODID}.$conf.pity_threshold" to "保底机制触发阈值",
                "${KTCMod.MODID}.$conf.world_gen" to "世界生成",
                "${KTCMod.MODID}.$conf.disc_box" to "抽奖机制",
                "${KTCMod.MODID}.$conf.key_mutator" to "KeyMutator玩法",
                "${KTCMod.MODID}.$conf.key_mutator.time_scale" to "KeyMutator玩法 - 时间缩放",
                "${KTCMod.MODID}.$conf.key_mutator.time_scale.tooltip" to "默认流程为 45 天（1 档 = 15 天）",
                "${KTCMod.MODID}.$conf.pity_threshold.tooltip" to "默认值：16",
                "${KTCMod.MODID}.$conf.world_gen.tooltip" to  "此选项控制世界生成相关的机制。",
                "${KTCMod.MODID}.$conf.pity_enabled.tooltip" to "此选项默认开启",
                "${KTCMod.MODID}.$conf.disc_box.tooltip" to "如果你自认为你是欧皇，那么可以考虑关闭它。",
                "${KTCMod.MODID}.$conf.disc_box.button" to "修改",
                "${KTCMod.MODID}.$conf.world_gen.button" to "修改",
                "${KTCMod.MODID}.$conf.title" to "兽娘红茶馆工艺 - 配置",
                "${KTCMod.MODID}.$conf.section.kemono.teatime.common.toml" to "兽娘红茶馆工艺 - 配置",
                "${KTCMod.MODID}.$conf.section.kemono.teatime.common.toml.title" to "兽娘红茶馆工艺 - 配置",
            )

            for (miscItem in ItemRegistries.KTCModItems.entries) {
                translations["item.${KTCMod.MODID}.${miscItem.registryName}"] = chineseMiscName(miscItem)
            }

            for (disc in DiscTrack.entries) {
                translations["item.${KTCMod.MODID}.${disc.itemName}"] = disc.displayName
                translations["item.${KTCMod.MODID}.${disc.itemName}.desc"] = "《兽娘红茶馆》原声带集 - ${disc.displayName}"
            }
            return translations
        }

        private enum class DiscTrack(val index: Int, val displayName: String) {
            DISC_1(1, "01 - A cup of happiness"),
            DISC_2(2, "02 - Darjeeling"),
            DISC_3(3, "03 - いつもの"),
            DISC_4(4, "04 - しとしと"),
            DISC_5(5, "05 - ミステリー"),
            DISC_6(6, "06 - 雪の日"),
            DISC_7(7, "07 - オーロラ"),
            DISC_8(8, "08 - タイトル"),
            DISC_9(9, "09 - あさのじゅんび"),
            DISC_10(10, "10 - ちょうせん"),
            DISC_11(11, "11 - 夜空の下で"),
            DISC_12(12, "12 - 回想"),
            DISC_13(13, "13 - あなたと"),
            DISC_14(14, "14 - くすり"),
            DISC_15(15, "15 - 追憶"),
            DISC_16(16, "16 - Betrayal"),
            DISC_17(17, "17 - 星空を見上げて"),
            DISC_18(18, "18 - 夢の島"),
            DISC_19(19, "19 - おもいで"),
            DISC_20(20, "20 - Uncontrollable"),
            DISC_21(21, "21 - みゃう・とぅー・へゔん");

            val itemName: String
                get() = "ost_disc_$index"
        }

        private fun englishMiscName(item: ItemRegistries.KTCModItems): String = when (item) {
            ItemRegistries.KTCModItems.BISCUIT -> "Biscuit"
            ItemRegistries.KTCModItems.TOOLKIT_1 -> "Beginner's Toolkit"
            ItemRegistries.KTCModItems.TOOLKIT_2 -> "Intermediate Toolkit"
            ItemRegistries.KTCModItems.TOOLKIT_3 -> "Advanced Toolkit"
            ItemRegistries.KTCModItems.TOOLKIT_4 -> "Superior Toolkit"
            ItemRegistries.KTCModItems.TABLET -> "Vitamin Tablet"
            ItemRegistries.KTCModItems.COIN -> "Deprecated Coin"
        }

        private fun chineseMiscName(item: ItemRegistries.KTCModItems): String = when (item) {
            ItemRegistries.KTCModItems.BISCUIT -> "饼干"
            ItemRegistries.KTCModItems.TOOLKIT_1 -> "新手工具包"
            ItemRegistries.KTCModItems.TOOLKIT_2 -> "中级工具包"
            ItemRegistries.KTCModItems.TOOLKIT_3 -> "高级工具包"
            ItemRegistries.KTCModItems.TOOLKIT_4 -> "卓越工具包"
            ItemRegistries.KTCModItems.TABLET -> "维生素片"
            ItemRegistries.KTCModItems.COIN -> "废弃硬币"
        }

        private const val conf: String = "configuration"
    }
}
