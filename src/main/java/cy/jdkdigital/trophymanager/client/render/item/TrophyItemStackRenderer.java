package cy.jdkdigital.trophymanager.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import cy.jdkdigital.trophymanager.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

public class TrophyItemStackRenderer extends BlockEntityWithoutLevelRenderer
{
    WeakReference<TrophyBlockEntity> blockEntity;

    public TrophyItemStackRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext transformType, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLightIn, int packedUV) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return;
        }
        TrophyBlockEntity weakBlockEntity = blockEntity != null ? blockEntity.get() : null;
        if (weakBlockEntity == null) {
            blockEntity = new WeakReference<>(new TrophyBlockEntity(BlockPos.ZERO, ModBlocks.TROPHY.get().defaultBlockState()));
            weakBlockEntity = blockEntity.get();
        }

        if (weakBlockEntity != null) {
            weakBlockEntity.loadData(stack.get(DataComponents.CUSTOM_DATA).copyTag(), Minecraft.getInstance().level.registryAccess());
            weakBlockEntity.isOnHead = transformType.equals(ItemDisplayContext.HEAD);

            poseStack.pushPose();

            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            poseStack.translate(-1.0f, 0, -1.0f);
            Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(weakBlockEntity, poseStack, buffer, packedLightIn, packedUV);

            poseStack.popPose();
        }
    }
}
