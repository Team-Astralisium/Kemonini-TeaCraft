package com.teamast.ktcmod.datagen

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.world.item.ItemRegistries
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.data.PackOutput

/**
 * 物品模型数据生成器
 */
class KTCItemModelProvider(output: PackOutput) : ModelProvider(output, KTCMod.MODID) {

    companion object {
        private const val DISC_COUNT = 21
    }

    override fun registerModels(blockModels: BlockModelGenerators, itemModels: ItemModelGenerators) {
        // OST盒模型
        itemModels.generateFlatItem(ItemRegistries.OST_BOX.get(), ModelTemplates.FLAT_ITEM)

        // 病毒携带体与使用后的空壳
        itemModels.generateFlatItem(ItemRegistries.CARRIER.get(), ModelTemplates.FLAT_ITEM)
        itemModels.generateFlatItem(ItemRegistries.EMPTY_CARRIER.get(), ModelTemplates.FLAT_ITEM)

        // 唱片模型
        for (index in 1..DISC_COUNT) {
            itemModels.generateFlatItem(ItemRegistries.getDisc(index).get(), ModelTemplates.FLAT_ITEM)
        }

        // 杂项物品模型
        for (miscItem in ItemRegistries.KTCModItems.entries) {
            itemModels.generateFlatItem(ItemRegistries.getMiscItem(miscItem).get(), ModelTemplates.FLAT_ITEM)
        }
    }
}
