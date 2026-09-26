package com.teamast.ktcmod.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.teamast.ktcmod.world.item.ItemRegistries;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 去掉病毒携带体食用动画的「后段抖动」。
 *
 * <p>原版 {@link ItemInHandRenderer} 的食用动画由私有方法 {@code applyEatTransform} 驱动：
 * <ol>
 *     <li>当 {@code 剩余时长 / 总时长 < 0.8}（也就是动画后 80%）时，每 4 tick 做一次
 *     {@code |cos(...)| * 0.1} 的竖直位移，即 EAT_JIGGLE（后段抖动）；</li>
 *     <li>之后按 {@code 1 - (剩余占比)^27} 把物品抬到嘴边。</li>
 * </ol>
 *
 * <p>这里只把携带体「第 1 次 translate」调用（即抖动位移）跳过，抬到嘴边的位移与旋转完全保留。
 * 粒子则由物品注册时 {@code CONSUMABLE} 组件的 {@code has_consume_particles=false} 关闭。
 *
 * <p>注意：Mixin 类必须用 Java 编写（Kotlin 的 final 类、属性访问器与缺失的注解处理会让
 * Mixin 的合并/refmap 处理出问题），因此本类位于 {@code src/main/java}。
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    /**
     * 参数说明：前 4 个参数是被包裹调用的 {@code PoseStack#translate} 的实例与实参，
     * {@code original} 是原始调用，其后按顺序是被注入方法 {@code applyEatTransform} 的全部形参
     * （renderPoseStack、frameInterp、arm、itemStack、player）。
     *
     * <p>注意 {@code applyEatTransform} 的形参必须从第 0 个开始完整列出（Mixin 只允许在末尾截断，
     * 不允许跳过），而它的第 0 个形参恰好也是同一个 {@code PoseStack}；因此这里会出现两个
     * {@code PoseStack} 形参——前者是 {@code translate} 的调用者，后者是被注入方法自己的形参，
     * 实际是同一个对象，只用前者即可。
     */
    @WrapOperation(
            method = "applyEatTransform",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                    ordinal = 0
            )
    )
    private void ktc$skipEatJiggle(
            PoseStack poseStack,
            float x,
            float y,
            float z,
            Operation<Void> original,
            PoseStack renderPoseStack,
            float frameInterp,
            HumanoidArm arm,
            ItemStack itemStack,
            Player player
    ) {
        if (itemStack.getItem() == ItemRegistries.CARRIER.get()) {
            return;
        }
        original.call(poseStack, x, y, z);
    }
}
