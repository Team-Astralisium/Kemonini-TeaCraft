package com.teamast.ktcmod.client.renderer

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.world.effect.virus.KeyMutatorStage
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.resources.Identifier
import net.minecraft.util.context.ContextKey
import net.minecraft.world.entity.LivingEntity
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderLivingEvent
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent
import java.util.function.BiConsumer

/**
 * 把 KeyMutator 的病程阶段送到客户端渲染状态，并挂上占位 renderer。
 *
 * 26.1 的渲染状态里不带实体引用，所以阶段由 NeoForge 官方的
 * [RegisterRenderStateModifiersEvent] 钩子在「抽取渲染状态」时写入
 * （对 [LivingEntityRenderer] 注册即可覆盖所有生物渲染器，不需要 Mixin），
 * 渲染时再从状态里读出来交给 [KeyMutatorPlaceholderRenderer]。
 */
@EventBusSubscriber(modid = KTCMod.MODID, value = [Dist.CLIENT])
object KeyMutatorClientRenderer {

    /** 渲染状态里携带的病程阶段。 */
    @JvmField
    val KEY_MUTATOR_STAGE: ContextKey<KeyMutatorStage> =
        ContextKey(Identifier.fromNamespaceAndPath(KTCMod.MODID, "key_mutator_stage"))

    /** 注册渲染状态修饰器：只在有 KeyMutator 时写入阶段，其余情况写入 null（等于清除）。 */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @SubscribeEvent
    fun onRegisterRenderStateModifiers(event: RegisterRenderStateModifiersEvent) {
        val rendererClass = LivingEntityRenderer::class.java
            as Class<EntityRenderer<LivingEntity, LivingEntityRenderState>>
        val modifier = BiConsumer<LivingEntity, LivingEntityRenderState> { entity, state ->
            state.setRenderData(KEY_MUTATOR_STAGE, KeyMutatorStage.of(entity))
        }
        event.registerEntityModifier(rendererClass, modifier)
    }

    /** 渲染前把阶段交给占位 renderer（真模型未实现，目前什么都不画）。 */
    @JvmStatic
    @SubscribeEvent
    fun onRenderLiving(event: RenderLivingEvent.Pre<*, *, *>) {
        val stage = event.renderState.getRenderData(KEY_MUTATOR_STAGE) ?: return
        KeyMutatorPlaceholderRenderer.render(event, stage)
    }
}
