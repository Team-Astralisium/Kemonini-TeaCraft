package com.teamast.ktcmod.world.entity.k

import com.teamast.ktcmod.world.entity.peddler.Peddler
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.level.Level
import java.util.EnumSet
import java.util.UUID

class K(entityType: EntityType<out K>, level: Level) : PathfinderMob(entityType, level) {
    // 跟随逻辑
    private var leaderUuid: UUID? = null
    private var cachedLeader: Peddler? = null
    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(PAIRING_TAG, "")
    }
    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, FollowLinkedPeddlerGoal(this, 1.15, 3.0f, 20.0f))
    }
    fun setPairingTag(tag: String) {
        entityData.set(PAIRING_TAG, tag)
    }
    override fun aiStep() {
        super.aiStep()
        if (level().isClientSide) {
            return
        }
        if (leaderUuid == null) {
            tryBindByPairingTag()
        }
    }
    private fun tryBindByPairingTag() {
        val tag = getPairingTag()
        if (tag.isEmpty()) {
            return
        }
        val serverLevel = level() as? ServerLevel ?: return
        val searchBox = boundingBox.inflate(64.0)
        val matched = serverLevel.getEntitiesOfClass(Peddler::class.java, searchBox) { it.entityTags().contains(tag) }.firstOrNull() ?: return
        leaderUuid = matched.uuid
        cachedLeader = matched
        matched.removeTag(tag)
        entityData.set(PAIRING_TAG, "")
    }
    fun getLinkedPeddler(): Peddler? {
        val cached = cachedLeader
        if (cached != null && cached.isAlive) {
            return cached
        }
        val uuid = leaderUuid ?: return null
        val serverLevel = level() as? ServerLevel ?: return null
        val fromWorld = serverLevel.getEntity(uuid) as? Peddler
        cachedLeader = fromWorld
        return fromWorld
    }

    companion object {
        const val DEFAULT_NAME: String = "tarte"
        const val PAIR_TAG_PREFIX: String = "ktc_pair_"
        private val PAIRING_TAG: EntityDataAccessor<String> = SynchedEntityData.defineId(K::class.java, EntityDataSerializers.STRING)
    }

    private fun getPairingTag(): String = entityData.get(PAIRING_TAG)
}

private class FollowLinkedPeddlerGoal(
    private val k: K,
    private val speed: Double,
    private val stopDistance: Float,
    private val startDistance: Float
) : Goal() {
    private var target: Peddler? = null

    init {
        flags = EnumSet.of(Flag.MOVE, Flag.LOOK)
    }

    override fun canUse(): Boolean {
        val leader = k.getLinkedPeddler() ?: return false
        if (!leader.isAlive) {
            return false
        }
        if (k.distanceToSqr(leader) < (startDistance * startDistance).toDouble()) {
            return false
        }
        target = leader
        return true
    }

    override fun canContinueToUse(): Boolean {
        val leader = target ?: return false
        if (!leader.isAlive) {
            return false
        }
        return k.distanceToSqr(leader) > (stopDistance * stopDistance).toDouble()
    }

    override fun tick() {
        val leader = target ?: return
        k.lookControl.setLookAt(leader, 10.0f, k.maxHeadXRot.toFloat())
        k.navigation.moveTo(leader, speed)
    }

    override fun stop() {
        target = null
        k.navigation.stop()
    }
}
