package com.teamast.ktcmod.world.loot

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.KTCModConfig
import com.teamast.ktcmod.world.item.ItemRegistries
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.LootTableLoadEvent

/**
 * 把病毒携带体塞进所有箱子战利品表。
 *
 * 实现方式：监听 [LootTableLoadEvent]（战利品表反序列化时触发，此时表还没冻结，
 * 可以安全地 addPool），只要表 id 的路径以 `chests/` 开头就追加一个独立的抽取池。
 *
 * 原版 56 张箱子表全部位于 `chests/` 下，其它模组只要沿用同样的命名也能覆盖到。
 * 之所以不用全局战利品修饰符（GLM）：`neoforge:loot_table_id` 只能精确匹配单个表 id，
 * 想覆盖“任何箱子”就得枚举几十张表或自定义条件，反而更麻烦。
 */
@EventBusSubscriber(modid = KTCMod.MODID)
object CarrierChestLoot {
    /** 箱子战利品表所在目录。 */
    private const val CHEST_TABLE_PREFIX = "chests/"

    /** 池名必须唯一，NeoForge 会拒绝向同一张表加入重名池。 */
    private const val POOL_NAME = "${KTCMod.MODID}:carrier"

    @JvmStatic
    @SubscribeEvent
    fun onLootTableLoad(event: LootTableLoadEvent) {
        if (!event.name.path.startsWith(CHEST_TABLE_PREFIX)) return

        val chance = KTCModConfig.CARRIER_CHEST_CHANCE.get().toFloat()
        if (chance <= 0.0F) return

        val pool = LootPool.lootPool()
            .name(POOL_NAME)
            .setRolls(ConstantValue.exactly(1.0F))
            .add(
                LootItem.lootTableItem(ItemRegistries.CARRIER.get())
                    .`when`(LootItemRandomChanceCondition.randomChance(chance))
            )
            .build()

        event.table.addPool(pool)
    }
}