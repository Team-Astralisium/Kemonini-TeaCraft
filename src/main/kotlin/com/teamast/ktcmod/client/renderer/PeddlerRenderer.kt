package com.teamast.ktcmod.client.renderer

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.world.entity.peddler.Peddler
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier

class PeddlerRenderer(context: EntityRendererProvider.Context) :
    HumanoidMobRenderer<Peddler, HumanoidRenderState, HumanoidModel<HumanoidRenderState>>(
        context,
        HumanoidModel(context.bakeLayer(ModelLayers.PLAYER), RenderTypes::entityCutout),
        HumanoidModel(context.bakeLayer(ModelLayers.PLAYER), RenderTypes::entityCutout),
        0.5f
    ) {

    override fun getTextureLocation(state: HumanoidRenderState): Identifier {
        return Identifier.fromNamespaceAndPath(KTCMod.MODID, "textures/entity/peddler.png")
    }

    override fun createRenderState(): HumanoidRenderState {
        return HumanoidRenderState()
    }
}