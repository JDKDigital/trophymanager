package cy.jdkdigital.trophymanager.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

public class TrophyBlockEntityRenderer implements BlockEntityRenderer<TrophyBlockEntity>
{
    private PlayerInfo playerInfo;
    PlayerModel<Player> playerModelRegular;
    PlayerModel<Player> playerModelSlim;

    public TrophyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.playerModelRegular = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.playerModelSlim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public void render(@Nonnull TrophyBlockEntity trophyTileEntity, float v, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        if (trophyTileEntity.trophyType != null && Minecraft.getInstance().level != null) {
            trophyTileEntity.setLevel(Minecraft.getInstance().level);
            if (trophyTileEntity.isOnHead) {
                poseStack.translate(0, 0.4f, 0);
            }
            if (trophyTileEntity.trophyType.equals("item") && trophyTileEntity.item != null) {
                renderItem(trophyTileEntity, poseStack, buffer, combinedLightIn, combinedOverlayIn);
            } else if (trophyTileEntity.trophyType.equals("entity")) {
                Entity entity = trophyTileEntity.getCachedEntity();
                if (entity != null) {
                    renderEntity(trophyTileEntity, poseStack, buffer, combinedLightIn);
                }
            }
        }

        renderBase(trophyTileEntity, poseStack, buffer, combinedLightIn, combinedOverlayIn);
    }

    private void renderBase(TrophyBlockEntity trophyTileEntity, PoseStack poseStack, @Nonnull MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        Block baseBlock = trophyTileEntity.isOnHead ? Blocks.AIR : trophyTileEntity.getBaseBlock();
        if (baseBlock != null) {
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(baseBlock.defaultBlockState(), poseStack, buffer, combinedLightIn, combinedOverlayIn);
        }
    }

    private void renderItem(TrophyBlockEntity trophyBlockEntity, PoseStack poseStack, @Nonnull MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        double tick = 0;
        if (TrophyManagerConfig.GENERAL.rotateItemTrophies.get() && !(trophyBlockEntity.item.getItem() instanceof BlockItem)) {
            tick = System.currentTimeMillis() / 800.0D;
        } else {
            if (trophyBlockEntity.getLevel() != null) {
                Direction facing = trophyBlockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
                if (facing == Direction.NORTH) {
                    tick = 6D;
                } else if (facing == Direction.SOUTH) {
                    tick = 0D;
                } else if (facing == Direction.EAST) {
                    tick = 3D;
                } else if (facing == Direction.WEST) {
                    tick = 9D;
                }
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, trophyBlockEntity.offsetY + 0.5D + Math.sin(tick / 25f) / 15f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) ((tick * 30.0D) % 360)));
        poseStack.scale(trophyBlockEntity.scale, trophyBlockEntity.scale, trophyBlockEntity.scale);
        if (trophyBlockEntity.item.getItem() instanceof BlockItem) {
            poseStack.translate(0, -0.25f, 0);
            poseStack.scale(3f, 3f, 3f);
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(trophyBlockEntity.item, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, poseStack, buffer, trophyBlockEntity.getLevel(), 0);
        poseStack.popPose();
    }

    private void renderEntity(TrophyBlockEntity trophyTileEntity, PoseStack poseStack, @Nonnull MultiBufferSource buffer, int combinedLightIn) {
        float angle = 0;
        if (trophyTileEntity.getLevel() != null) {
            Direction facing = trophyTileEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            if (facing == Direction.NORTH) {
                angle = 180f;
            } else if (facing == Direction.SOUTH) {
                angle = 0f;
            } else if (facing == Direction.EAST) {
                angle = 90f;
            } else if (facing == Direction.WEST) {
                angle = 270f;
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, trophyTileEntity.offsetY, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.mulPose(Axis.XP.rotationDegrees(trophyTileEntity.rotX));
        poseStack.scale(trophyTileEntity.scale, trophyTileEntity.scale, trophyTileEntity.scale);

        if (trophyTileEntity.entity.getString("entityType").equals("minecraft:ender_dragon")) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        }

        EntityRenderDispatcher entityRendererManager = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRendererManager.setRenderShadow(false);
        Entity cachedEntity = trophyTileEntity.getCachedEntity();
        if (cachedEntity != null) {
            try {
                entityRendererManager.render(cachedEntity, 0, 0, 0., 1, 1, poseStack, buffer, combinedLightIn);
                renderPassengers(cachedEntity, entityRendererManager, poseStack, buffer, combinedLightIn);
                if (buffer instanceof MultiBufferSource.BufferSource bufferSource) {
                    bufferSource.endBatch();
                }
            } catch (Exception e) {
                // too bad
            }
        }

        poseStack.popPose();
    }

    private static void renderPassengers(Entity entity, EntityRenderDispatcher entityRendererManager, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn) {
        if (entity.isVehicle()) {
            for(Entity rider : entity.getPassengers()) {
                entity.positionRider(rider);
                entityRendererManager.render(rider, rider.getX(), rider.getY(), rider.getZ(), Minecraft.getInstance().getFrameTimeNs(), 1, matrixStack, buffer, combinedLightIn);
                renderPassengers(rider, entityRendererManager, matrixStack, buffer, combinedLightIn);
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TrophyBlockEntity pBlockEntity) {
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
