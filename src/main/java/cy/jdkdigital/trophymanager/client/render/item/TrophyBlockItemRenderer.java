package cy.jdkdigital.trophymanager.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import cy.jdkdigital.trophymanager.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class TrophyBlockItemRenderer implements SpecialModelRenderer<CompoundTag>
{
    private static final BlockDisplayContext BASE_CONTEXT = BlockDisplayContext.create();
    private static final CameraRenderState DUMMY_CAMERA = new CameraRenderState();

    private final TrophyBlockEntity scratch = new TrophyBlockEntity(BlockPos.ZERO, ModBlocks.TROPHY.get().defaultBlockState());
    private final ItemStackRenderState itemRenderState = new ItemStackRenderState();
    private final BlockModelRenderState baseModelState = new BlockModelRenderState();
    private final boolean onHead;

    public TrophyBlockItemRenderer(boolean onHead) {
        this.onHead = onHead;
    }

    @Override
    public @Nullable CompoundTag extractArgument(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    @Override
    public void submit(@Nullable CompoundTag tag, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.translate(-1.0f, 0, -1.0f);
        submitTrophy(tag, poseStack, collector, lightCoords, overlayCoords);
        poseStack.popPose();
    }

    public void submitTrophy(@Nullable CompoundTag tag, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords) {
        Minecraft mc = Minecraft.getInstance();
        if (tag == null || mc.level == null) {
            return;
        }
        scratch.setLevel(mc.level);
        scratch.loadData(tag, mc.level.registryAccess());
        scratch.isOnHead = this.onHead;

        poseStack.pushPose();
        if (this.onHead) {
            poseStack.translate(0, 0.4f, 0);
        }

        if ("item".equals(scratch.trophyType) && scratch.item != null && !scratch.item.isEmpty()) {
            submitItem(mc, poseStack, collector, lightCoords, overlayCoords);
        } else if ("entity".equals(scratch.trophyType)) {
            submitEntity(mc, poseStack, collector);
        }

        if (!this.onHead) {
            Block base = scratch.getBaseBlock();
            if (base != null) {
                mc.getBlockModelResolver().update(baseModelState, base.defaultBlockState(), BASE_CONTEXT);
                baseModelState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }
        }

        poseStack.popPose();
    }

    private void submitItem(Minecraft mc, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords) {
        boolean isBlock = scratch.item.getItem() instanceof BlockItem;
        double tick = TrophyManagerConfig.GENERAL.rotateItemTrophies.get() && !isBlock ? System.currentTimeMillis() / 800.0D : 0D;
        mc.getItemModelResolver().updateForTopItem(itemRenderState, scratch.item, ItemDisplayContext.FIXED, mc.level, null, 0);

        poseStack.pushPose();
        poseStack.translate(0.5f, scratch.offsetY + 0.5D + Math.sin(tick / 25f) / 15f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) ((tick * 30.0D) % 360)));
        poseStack.scale(scratch.scale, scratch.scale, scratch.scale);
        if (isBlock) {
            poseStack.translate(0, -0.25f, 0);
            poseStack.scale(3f, 3f, 3f);
        }
        itemRenderState.submit(poseStack, collector, lightCoords, overlayCoords, 0);
        poseStack.popPose();
    }

    private void submitEntity(Minecraft mc, PoseStack poseStack, SubmitNodeCollector collector) {
        Entity entity = scratch.getCachedEntity();
        if (entity == null) {
            return;
        }
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        EntityRenderState renderState = dispatcher.extractEntity(entity, 0.0F);
        renderState.shadowRadius = 0;

        poseStack.pushPose();
        poseStack.translate(0.5f, scratch.offsetY, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.mulPose(Axis.XP.rotationDegrees(scratch.rotX));
        poseStack.scale(scratch.scale, scratch.scale, scratch.scale);
        if (scratch.entity != null && "minecraft:ender_dragon".equals(scratch.entity.getStringOr("entityType", ""))) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        }
        dispatcher.submit(renderState, DUMMY_CAMERA, 0, 0, 0, poseStack, collector);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0.0F, 0.0F, 0.0F));
        output.accept(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    public record Unbaked(boolean onHead) implements SpecialModelRenderer.Unbaked<CompoundTag>
    {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(TrophyManager.MODID, "trophy");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(Codec.BOOL.optionalFieldOf("on_head", false).forGetter(Unbaked::onHead)).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<CompoundTag> bake(SpecialModelRenderer.BakingContext context) {
            return new TrophyBlockItemRenderer(this.onHead);
        }
    }
}
