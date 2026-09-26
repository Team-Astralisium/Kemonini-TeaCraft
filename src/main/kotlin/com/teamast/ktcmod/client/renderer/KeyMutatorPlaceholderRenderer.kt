package com.teamast.ktcmod.client.renderer

import com.teamast.ktcmod.world.effect.virus.KeyMutatorStage
import net.neoforged.neoforge.client.event.RenderLivingEvent

/**
 * 按 [KeyMutatorStage] 更换效果获得者模型的**占位 renderer**。
 *
 * 目前 5 套模型都还没做，所以这里只保留挂载点：[render] 什么都不做。
 * 模型就绪后的接法：
 * 1. 先在 [stageModel] 里给每个阶段配上模型/贴图资源；
 * 2. 在 [render] 中 `event.isCanceled = true` 取消原版渲染，再用 `event.poseStack` /
 *    `event.submitNodeCollector` 提交对应阶段的模型。
 *
 * 阶段数据由 `KeyMutatorClientRenderer` 在渲染状态里提供（`state.getRenderData(...)`），
 * 因此这里不需要再碰实体本身，玩家与非玩家生物一视同仁。
 */
object KeyMutatorPlaceholderRenderer {

    /** 阶段 → 模型资源（TODO：模型做好后填写，例如 `kemono_teatime:entity/key_mutator_stage_1`）。 */
    private val stageModels: Map<KeyMutatorStage, String> = emptyMap()

    /**
     * 渲染入口：由 `KeyMutatorClientRenderer` 在 [RenderLivingEvent.Pre] 里调用。
     *
     * @param event 当前被渲染的生物
     * @param stage 该生物身上的 KeyMutator 阶段
     */
    @JvmStatic
    fun render(event: RenderLivingEvent.Pre<*, *, *>, stage: KeyMutatorStage) {
        // TODO 真实模型：stageModels[stage] 不为空时，取消原版渲染并换成对应模型
        if (stageModels[stage] == null) {
            return
        }
    }
}
