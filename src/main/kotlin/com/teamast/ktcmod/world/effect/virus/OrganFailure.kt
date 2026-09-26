package com.teamast.ktcmod.world.effect.virus

import com.teamast.ktcmod.KTCMod
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.LivingEntity

/**
 * 「器官衰竭」致死手段。
 *
 * 伤害类型本体是数据包文件 `data/kemono_teatime/damage_type/organ_failure.json`，
 * 通过 `message_id` 指定死亡信息（`death.attack.kemono_teatime.organ_failure`），
 * 并借助 `data/minecraft/tags/damage_type/` 下的合并 tag 让它像魔法伤害一样无视护甲，
 * 且（和 /kill 一样）无视无敌状态——否则创造模式玩家不会死。
 */
object OrganFailure {
    @JvmField
    val DAMAGE_TYPE: ResourceKey<DamageType> = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        Identifier.fromNamespaceAndPath(KTCMod.MODID, "organ_failure")
    )

    /** 构造伤害来源。 */
    @JvmStatic
    fun source(level: ServerLevel): DamageSource =
        DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DAMAGE_TYPE))

    /** 让实体因器官衰竭死亡。 */
    @JvmStatic
    fun kill(entity: LivingEntity) {
        val level = entity.level() as? ServerLevel ?: return
        entity.hurtServer(level, source(level), Float.MAX_VALUE)
    }
}
