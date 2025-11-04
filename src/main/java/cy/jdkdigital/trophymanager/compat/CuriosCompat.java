package cy.jdkdigital.trophymanager.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import cy.jdkdigital.trophymanager.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.InterModComms;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CuriosCompat
{
    public static void register() {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HEAD.getMessageBuilder().build());

        CuriosRendererRegistry.register(ModBlocks.TROPHY.get().asItem(), Renderer::new);
    }

    public static class Renderer implements ICurioRenderer
    {
        TrophyBlockEntity blockEntity = null;

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int packedLightIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!itemStack.has(DataComponents.CUSTOM_DATA)) {
                return;
            }
            if (blockEntity == null) {
                blockEntity = new TrophyBlockEntity(BlockPos.ZERO, ModBlocks.TROPHY.get().defaultBlockState());
            }
            blockEntity.loadData(itemStack.get(DataComponents.CUSTOM_DATA).copyTag(), Minecraft.getInstance().level.registryAccess());

            blockEntity.isOnHead = false;
            if (slotContext.identifier().equals("head")) {
                blockEntity.isOnHead = true;
            }

            var contextModel = renderLayerParent.getModel();
            if (!(contextModel instanceof HumanoidModel<?> humanoidModel)) {
                return;
            }

            poseStack.pushPose();

            humanoidModel.head.translateAndRotate(poseStack);
            poseStack.translate(-0.35, 0.15, -0.35);
            poseStack.scale(0.70F, -0.70F, 0.70F);

            Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(blockEntity, poseStack, multiBufferSource, packedLightIn, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }
    }
}
