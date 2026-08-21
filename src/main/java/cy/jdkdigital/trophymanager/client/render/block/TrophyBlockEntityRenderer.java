package cy.jdkdigital.trophymanager.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.client.render.block.state.TrophyRenderState;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class TrophyBlockEntityRenderer implements BlockEntityRenderer<TrophyBlockEntity, TrophyRenderState>
{
    private static final BlockDisplayContext BASE_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemModelResolver itemModelResolver;
    private final BlockModelResolver blockModelResolver;

    public TrophyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderDispatcher = context.entityRenderer();
        this.itemModelResolver = context.itemModelResolver();
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public TrophyRenderState createRenderState() {
        return new TrophyRenderState();
    }

    @Override
    public void extractRenderState(TrophyBlockEntity be, TrophyRenderState state, float partialTick, @Nonnull Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);

        if (be.getLevel() == null && Minecraft.getInstance().level != null) {
            be.setLevel(Minecraft.getInstance().level);
        }

        state.isOnHead = be.isOnHead;
        state.offsetY = be.offsetY;
        state.rotX = be.rotX;
        state.scale = be.scale;
        state.renderItem = false;
        state.entityRenderState = null;
        state.passengers.clear();
        state.hasBase = false;

        if ("item".equals(be.trophyType) && be.item != null && !be.item.isEmpty()) {
            state.renderItem = true;
            state.itemIsBlock = be.item.getItem() instanceof BlockItem;
            boolean rotate = TrophyManagerConfig.GENERAL.rotateItemTrophies.get() && !state.itemIsBlock;
            double tick;
            if (rotate) {
                tick = System.currentTimeMillis() / 800.0D;
            } else {
                tick = switch (facing(be)) {
                    case NORTH -> 6D;
                    case EAST -> 3D;
                    case WEST -> 9D;
                    default -> 0D;
                };
            }
            state.itemBob = Math.sin(tick / 25f) / 15f;
            state.itemSpin = (float) ((tick * 30.0D) % 360);
            this.itemModelResolver.updateForTopItem(state.itemRenderState, be.item, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
        } else if ("entity".equals(be.trophyType)) {
            Entity entity = be.getCachedEntity();
            if (entity != null) {
                state.isEnderDragon = be.entity != null && "minecraft:ender_dragon".equals(be.entity.getStringOr("entityType", ""));
                state.facingAngle = switch (facing(be)) {
                    case NORTH -> 180f;
                    case EAST -> 90f;
                    case WEST -> 270f;
                    default -> 0f;
                };
                state.entityRenderState = this.entityRenderDispatcher.extractEntity(entity, partialTick);
                state.entityRenderState.shadowRadius = 0;
                try {
                    extractPassengers(entity, state, partialTick);
                } catch (Exception ignored) {
                }
            }
        }

        if (!be.isOnHead) {
            Block baseBlock = be.getBaseBlock();
            if (baseBlock != null) {
                state.hasBase = true;
                this.blockModelResolver.update(state.baseModelState, baseBlock.defaultBlockState(), BASE_DISPLAY_CONTEXT);
            }
        }
    }

    private void extractPassengers(Entity vehicle, TrophyRenderState state, float partialTick) {
        if (vehicle.isVehicle()) {
            for (Entity rider : vehicle.getPassengers()) {
                vehicle.positionRider(rider);
                TrophyRenderState.Passenger p = new TrophyRenderState.Passenger();
                p.offset = new Vec3(rider.getX() - vehicle.getX(), rider.getY() - vehicle.getY(), rider.getZ() - vehicle.getZ());
                p.state = this.entityRenderDispatcher.extractEntity(rider, partialTick);
                p.state.shadowRadius = 0;
                state.passengers.add(p);
                extractPassengers(rider, state, partialTick);
            }
        }
    }

    private static Direction facing(TrophyBlockEntity be) {
        return be.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public void submit(TrophyRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState cameraState) {
        if (state.isOnHead) {
            poseStack.translate(0, 0.4f, 0);
        }

        if (state.renderItem) {
            poseStack.pushPose();
            poseStack.translate(0.5f, state.offsetY + 0.5D + state.itemBob, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.itemSpin));
            poseStack.scale(state.scale, state.scale, state.scale);
            if (state.itemIsBlock) {
                poseStack.translate(0, -0.25f, 0);
                poseStack.scale(3f, 3f, 3f);
            }
            state.itemRenderState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        } else if (state.entityRenderState != null) {
            poseStack.pushPose();
            poseStack.translate(0.5f, state.offsetY, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.facingAngle));
            poseStack.mulPose(Axis.XP.rotationDegrees(state.rotX));
            poseStack.scale(state.scale, state.scale, state.scale);
            if (state.isEnderDragon) {
                poseStack.mulPose(Axis.XP.rotationDegrees(180f));
                poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            }
            this.entityRenderDispatcher.submit(state.entityRenderState, cameraState, 0, 0, 0, poseStack, collector);
            for (TrophyRenderState.Passenger p : state.passengers) {
                poseStack.pushPose();
                poseStack.translate(p.offset.x, p.offset.y, p.offset.z);
                this.entityRenderDispatcher.submit(p.state, cameraState, 0, 0, 0, poseStack, collector);
                poseStack.popPose();
            }
            poseStack.popPose();
        }

        if (state.hasBase) {
            state.baseModelState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(TrophyBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
