package cy.jdkdigital.trophymanager.client.gui;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import cy.jdkdigital.trophymanager.common.entity.TrophyPose;
import cy.jdkdigital.trophymanager.compat.CobblemonCompat;
import cy.jdkdigital.trophymanager.init.ModEntities;
import cy.jdkdigital.trophymanager.network.PacketUpdateTrophy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Consumer;

public class TrophyScreen extends Screen
{
    private static final int WIDTH = 150;
    private static final int TITLE_ROW = 4;
    private static final int SCALE_ROW = 20;
    private static final int OFFSET_ROW = 45;
    private static final int POSE_ROW = 70;
    private static final int ROW_HEIGHT = 25;
    private static final int LABEL_INSET = 6;
    private static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(TrophyManager.MODID, "textures/gui/trophy.png");

    private final TrophyBlockEntity trophy;
    private int panelLeft;
    private int panelTop;
    private int panelHeight;
    private int sliderTop;

    protected TrophyScreen(TrophyBlockEntity trophy) {
        super(trophy.getDisplayName());
        this.trophy = trophy;
    }

    @Override
    protected void init() {
        boolean poseControls = hasPoseControls();
        this.sliderTop = poseControls ? POSE_ROW + ROW_HEIGHT : POSE_ROW;
        this.panelHeight = sliderTop + 95;
        this.panelLeft = (this.width - WIDTH) / 2;
        this.panelTop = (this.height - panelHeight) / 2;

        addRenderableWidget(Button.builder(Component.literal("-"), button -> adjustScale(-1)).pos(panelLeft + 10, panelTop + SCALE_ROW).size(20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> adjustScale(1)).pos(panelLeft + 120, panelTop + SCALE_ROW).size(20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("-"), button -> adjustOffsetY(-1)).pos(panelLeft + 10, panelTop + OFFSET_ROW).size(20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> adjustOffsetY(1)).pos(panelLeft + 120, panelTop + OFFSET_ROW).size(20, 20).build());

        if (poseControls) {
            addRenderableWidget(Button.builder(Component.literal("<"), button -> changePose(-1)).pos(panelLeft + 10, panelTop + POSE_ROW).size(20, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"), button -> changePose(1)).pos(panelLeft + 120, panelTop + POSE_ROW).size(20, 20).build());
        }

        addRenderableWidget(new RotationSlider(panelLeft + 10, panelTop + sliderTop, 130, "X", trophy.rotX, v -> trophy.rotX = v));
        addRenderableWidget(new RotationSlider(panelLeft + 10, panelTop + sliderTop + 22, 130, "Y", trophy.rotY, v -> trophy.rotY = v));
        addRenderableWidget(new RotationSlider(panelLeft + 10, panelTop + sliderTop + 44, 130, "Z", trophy.rotZ, v -> trophy.rotZ = v));

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> close()).pos(panelLeft + 10, panelTop + sliderTop + 69).size(65, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.apply"), button -> save(this)).pos(panelLeft + 76, panelTop + sliderTop + 69).size(65, 20).build());
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {
        guiGraphics.blit(GUI, panelLeft, panelTop, 0, 0, WIDTH, panelHeight);

        guiGraphics.drawCenteredString(font, getTitle(), panelLeft + 75, panelTop + TITLE_ROW, 16777215);
        guiGraphics.drawCenteredString(font, Component.translatable("gui.trophy.size", trophy.scale), panelLeft + 75, panelTop + SCALE_ROW + LABEL_INSET, 14737632);
        guiGraphics.drawCenteredString(font, Component.translatable("gui.trophy.offset", trophy.offsetY), panelLeft + 75, panelTop + OFFSET_ROW + LABEL_INSET, 14737632);
        if (hasPoseControls()) {
            guiGraphics.drawCenteredString(font, poseLabel(), panelLeft + 75, panelTop + POSE_ROW + LABEL_INSET, 14737632);
        }
        super.renderMenuBackground(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void adjustScale(float d) {
        if (Screen.hasShiftDown()) {
            d = d * 10;
        }
        trophy.scale = (float) Math.round(trophy.scale * 10 + d) / 10f;
        if (trophy.scale > TrophyManagerConfig.GENERAL.maxSize.get()) {
            trophy.scale = TrophyManagerConfig.GENERAL.maxSize.get().floatValue();
        }
        if (trophy.scale < TrophyManagerConfig.GENERAL.maxSize.get() * -1) {
            trophy.scale = TrophyManagerConfig.GENERAL.maxSize.get().floatValue() * -1;
        }
    }

    private void adjustOffsetY(double d) {
        if (Screen.hasShiftDown()) {
            d = d * 10;
        }
        trophy.offsetY = Math.round(trophy.offsetY * 10 + d) / 10d;
        if (trophy.offsetY > TrophyManagerConfig.GENERAL.maxYOffset.get()) {
            trophy.offsetY = TrophyManagerConfig.GENERAL.maxYOffset.get();
        }
        if (trophy.offsetY < TrophyManagerConfig.GENERAL.maxYOffset.get() * -1) {
            trophy.offsetY = TrophyManagerConfig.GENERAL.maxYOffset.get() * -1;
        }
    }

    private void changePose(int direction) {
        if (isPokemonTrophy()) {
            CobblemonCompat.changePose(trophy, direction);
        } else if (isPlayerTrophy()) {
            trophy.entity.putString(TrophyPose.NBT_KEY, playerPose().cycle(direction).name());
        }
    }

    private Component poseLabel() {
        return Component.translatable("gui.trophy.pose", isPokemonTrophy() ? CobblemonCompat.getPose(trophy) : playerPose().label());
    }

    private TrophyPose playerPose() {
        return TrophyPose.byName(trophy.entity.getString(TrophyPose.NBT_KEY));
    }

    private boolean hasPoseControls() {
        return isPokemonTrophy() || isPlayerTrophy();
    }

    private boolean isPokemonTrophy() {
        return ModList.get().isLoaded("cobblemon") && CobblemonCompat.isPokemonTrophy(trophy);
    }

    private boolean isPlayerTrophy() {
        return "entity".equals(trophy.trophyType) && trophy.entity != null && trophy.entity.getString("entityType").equals(ModEntities.PLAYER.getId().toString());
    }

    public static void open(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        if (level != null && level.getBlockEntity(pos) instanceof TrophyBlockEntity trophy) {
            Minecraft.getInstance().setScreen(new TrophyScreen(trophy));
        }
    }

    public static void save(TrophyScreen screen) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("OffsetY", screen.trophy.offsetY);
        tag.putFloat("Scale", screen.trophy.scale);
        tag.putFloat("RotX", screen.trophy.rotX);
        tag.putFloat("RotY", screen.trophy.rotY);
        tag.putFloat("RotZ", screen.trophy.rotZ);
        if (screen.isPokemonTrophy()) {
            tag.putString("PoseType", screen.trophy.entity.getString("PoseType"));
        } else if (screen.isPlayerTrophy()) {
            tag.putString(TrophyPose.NBT_KEY, screen.playerPose().name());
        }
        PacketDistributor.sendToServer(new PacketUpdateTrophy(screen.trophy.getBlockPos(), tag));
        close();
    }

    public static void close() {
        Minecraft.getInstance().setScreen(null);
    }

    private static class RotationSlider extends AbstractSliderButton
    {
        private final String axis;
        private final Consumer<Float> apply;

        private RotationSlider(int x, int y, int width, String axis, float degrees, Consumer<Float> apply) {
            super(x, y, width, 20, Component.empty(), degrees / 360.0D);
            this.axis = axis;
            this.apply = apply;
            updateMessage();
        }

        private int degrees() {
            return (int) Math.round(this.value * 360.0D) % 360;
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.trophy.rotation", axis, degrees()));
        }

        @Override
        protected void applyValue() {
            apply.accept((float) degrees());
        }
    }
}
