package cy.jdkdigital.trophymanager.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.trophymanager.common.entity.RenderPlayer;
import cy.jdkdigital.trophymanager.common.entity.TrophyPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

public class PlayerTrophyRenderer extends LivingEntityRenderer<RenderPlayer, AvatarRenderState, PlayerModel>
{
    private static final float FLYING_PITCH = -90.0F;
    private static final float FLYING_TICKS = 20.0F;

    private final PlayerModel wideModel;
    private final PlayerModel slimModel;

    public PlayerTrophyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);

        this.wideModel = getModel();
        this.slimModel = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

        addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), part -> new PlayerModel(part, false)),
                context.getEquipmentRenderer()));
        addLayer(new PlayerItemInHandLayer<>(this));
    }

    @Override
    public @NotNull AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    public void extractRenderState(RenderPlayer entity, AvatarRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        state.skin = skin(entity);

        TrophyPose pose = entity.getTrophyPose();
        state.isPassenger = pose == TrophyPose.SITTING;
        state.isFallFlying = pose == TrophyPose.FLYING;
        state.fallFlyingTimeInTicks = state.isFallFlying ? FLYING_TICKS : 0.0F;
        state.shouldApplyFlyingYRot = false;
    }

    @Override
    public void submit(@NotNull AvatarRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
        this.model = state.skin.model() == PlayerModelType.SLIM ? slimModel : wideModel;
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected void setupRotations(@NotNull AvatarRenderState state, @NotNull PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);

        if (state.isFallFlying) {
            float half = state.boundingBoxHeight * 0.5F;
            poseStack.translate(0.0F, half, half);
            poseStack.mulPose(Axis.XP.rotationDegrees(FLYING_PITCH));
        }
    }

    @Override
    public @NotNull Identifier getTextureLocation(AvatarRenderState state) {
        return state.skin.body().texturePath();
    }

    private static PlayerSkin skin(RenderPlayer entity) {
        ResolvableProfile profile = entity.getProfile();
        if (profile == null) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
        try {
            return Minecraft.getInstance().playerSkinRenderCache().getOrDefault(profile).playerSkin();
        } catch (Exception e) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
    }
}
