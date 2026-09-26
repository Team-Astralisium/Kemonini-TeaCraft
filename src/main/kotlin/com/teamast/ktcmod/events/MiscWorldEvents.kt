package com.teamast.ktcmod.events

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.KTCMod.Companion.LOGGER
import com.teamast.ktcmod.KTCModConfig
import com.teamast.ktcmod.world.entity.EntityRegistries
import com.teamast.ktcmod.world.entity.k.K
import com.teamast.ktcmod.world.entity.peddler.Peddler
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.equine.TraderLlama
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent

@EventBusSubscriber(modid = KTCMod.MODID)
object MiscWorldEvents {

    @JvmStatic
    @SubscribeEvent
    fun onEntityJoinLevel(event: EntityJoinLevelEvent) {
        val entity = event.entity

        // Only replace vanilla wandering traders, Peddler must be ignored
        // to avoid recursive replacement loops when addFreshEntity triggers this event again.
        if (entity !is WanderingTrader || entity.type != EntityType.WANDERING_TRADER) {
            return
        }

        if (entity.isPassenger || entity.isVehicle) {
            return
        }

        if (KTCModConfig.REPLACE_WANDERING_TRADER_WITH_PEDDLER.get()) {
            replaceWanderingTraderWithPeddler(event, entity)
        }
    }

    private fun replaceWanderingTraderWithPeddler(event: EntityJoinLevelEvent, trader: WanderingTrader) {
        val level = trader.level()

        if (level !is ServerLevel) {
            return
        }

        val pos = trader.blockPosition()
        val peddler = EntityRegistries.PEDDLER.get().create(level, EntitySpawnReason.EVENT) ?: run {
            LOGGER.warn("Failed to create Peddler entity")
            return
        }

        peddler.setPos(trader.x, trader.y, trader.z)
        peddler.yRot = trader.yRot
        peddler.xRot = trader.xRot
        peddler.despawnDelay = trader.despawnDelay
        val pairTag = K.PAIR_TAG_PREFIX + peddler.uuid
        peddler.addTag(pairTag)

        removeTraderLlamas(level, trader)

        level.addFreshEntity(peddler)
        spawnCompanionK(level, trader, peddler, pairTag)
        trader.discard()

        event.isCanceled = true
        LOGGER.debug("Replaced Wandering Trader with Suichi at {}", pos)
    }

    private fun spawnCompanionK(level: ServerLevel, trader: WanderingTrader, peddler: Peddler, pairTag: String) {
        val companion = EntityRegistries.K.get().create(level, EntitySpawnReason.EVENT) ?: run {
            LOGGER.warn("Failed to create k_manager entity")
            peddler.removeTag(pairTag)
            return
        }

        companion.setPos(trader.x + 0.8, trader.y, trader.z + 0.8)
        companion.yRot = trader.yRot
        companion.xRot = trader.xRot
        companion.setCustomName(Component.literal(K.DEFAULT_NAME))
        companion.setPairingTag(pairTag)
        level.addFreshEntity(companion)
    }

    private fun removeTraderLlamas(level: ServerLevel, trader: WanderingTrader) {
        val searchRadius = 10
        val traderPos = trader.blockPosition()

        for (entity in level.getAllEntities()) {
            if (entity is TraderLlama) {
                val distance = entity.blockPosition().distSqr(traderPos)
                if (distance <= (searchRadius * searchRadius).toDouble()) {
                    entity.discard()
                    LOGGER.debug("Removed TraderLlama at {}", entity.blockPosition())
                }
            }
        }
    }
}
