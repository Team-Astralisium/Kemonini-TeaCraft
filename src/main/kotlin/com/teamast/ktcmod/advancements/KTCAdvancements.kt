package com.teamast.ktcmod.advancements

import com.teamast.ktcmod.KTCMod
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer

object KTCAdvancements {
    val FINAL_DISC_ID: Identifier = Identifier.fromNamespaceAndPath(KTCMod.MODID, "final_disc")
    val OST_FIRST_ID: Identifier = Identifier.fromNamespaceAndPath(KTCMod.MODID, "ost_first")
    val PEDDLER_MET_ID: Identifier = Identifier.fromNamespaceAndPath(KTCMod.MODID, "peddler_met")

    private const val MANUAL_CRITERION = "trigger"

    fun grantOstFirst(player: ServerPlayer) {
        grantById(player, OST_FIRST_ID)
    }

    fun grantPeddlerMet(player: ServerPlayer) {
        grantById(player, PEDDLER_MET_ID)
    }

    private fun grantById(player: ServerPlayer, advancementId: Identifier) {
        val holder = player.level().getServer().getAdvancements().get(advancementId) ?: return
        player.advancements.award(holder, MANUAL_CRITERION)
    }
}
