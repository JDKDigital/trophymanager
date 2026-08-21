package cy.jdkdigital.trophymanager.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import cy.jdkdigital.trophymanager.client.render.item.TrophyBlockItemRenderer;
import cy.jdkdigital.trophymanager.init.ModBlocks;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CuriosCompat
{
    public static void registerRenderer() {
        ICurioRenderer.register(ModBlocks.TROPHY.get().asItem(), Renderer::new);
    }

    public static class Renderer implements ICurioRenderer
    {
        private final TrophyBlockItemRenderer trophyRenderer = new TrophyBlockItemRenderer(true);

        @Override
        public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context, float netHeadYaw, float headPitch) {
            CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
            if (data == null) {
                return;
            }
            if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel)) {
                return;
            }

            poseStack.pushPose();

            humanoidModel.head.translateAndRotate(poseStack);
            poseStack.translate(-0.35, 0.15, -0.35);
            poseStack.scale(0.70F, -0.70F, 0.70F);

            trophyRenderer.submitTrophy(data.copyTag(), poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }
    }
}
