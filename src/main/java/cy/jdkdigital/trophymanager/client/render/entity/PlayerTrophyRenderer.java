package cy.jdkdigital.trophymanager.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.trophymanager.common.entity.RenderPlayer;
import cy.jdkdigital.trophymanager.common.entity.TrophyPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerTrophyRenderer extends LivingEntityRenderer<RenderPlayer, PlayerModel<RenderPlayer>>
{
    private static final float FLYING_PITCH = -90.0F;

    private final PlayerModel<RenderPlayer> wideModel;
    private final PlayerModel<RenderPlayer> slimModel;

    public PlayerTrophyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);

        this.wideModel = getModel();
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(@NotNull RenderPlayer entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        PlayerSkin skin = skin(entity);
        this.model = skin != null && skin.model() == PlayerSkin.Model.SLIM ? slimModel : wideModel;
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    protected void setupRotations(@NotNull RenderPlayer entity, @NotNull PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);

        TrophyPose pose = entity.getTrophyPose();
        this.model.riding = pose == TrophyPose.SITTING;
        if (pose == TrophyPose.FLYING) {
            float half = entity.getBbHeight() * 0.5F;
            poseStack.translate(0.0F, half, half);
            poseStack.mulPose(Axis.XP.rotationDegrees(FLYING_PITCH));
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RenderPlayer entity) {
        PlayerSkin skin = skin(entity);
        return skin == null ? DefaultPlayerSkin.getDefaultTexture() : skin.texture();
    }

    private static @Nullable PlayerSkin skin(RenderPlayer entity) {
        ResolvableProfile profile = entity.getProfile();
        if (profile == null) {
            return null;
        }
        try {
            return Minecraft.getInstance().getSkinManager().getInsecureSkin(profile.gameProfile());
        } catch (Exception e) {
            return null;
        }
    }
}
