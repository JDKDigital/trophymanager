package cy.jdkdigital.trophymanager.client.gui;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import cy.jdkdigital.trophymanager.common.datamap.PropertiesMap;
import cy.jdkdigital.trophymanager.common.entity.TrophyPose;
import cy.jdkdigital.trophymanager.init.ModEntities;
import cy.jdkdigital.trophymanager.network.PacketUpdateTrophy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

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
    private static final int LABEL_COLOR = 0xFFE0E0E0;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final Identifier GUI = Identifier.fromNamespaceAndPath(TrophyManager.MODID, "textures/gui/trophy.png");

    private final TrophyBlockEntity trophy;

    private final double initialOffsetY;
    private final float initialScale;
    private final float initialRotX;
    private final float initialRotY;
    private final float initialRotZ;
    private final TrophyPose initialPose;

    private int panelLeft;
    private int panelTop;
    private int panelHeight;
    private int sliderTop;

    protected TrophyScreen(TrophyBlockEntity trophy) {
        super(trophy.getDisplayName());
        this.trophy = trophy;

        initialOffsetY = trophy.offsetY;
        initialScale = trophy.scale;
        initialRotX = trophy.rotX;
        initialRotY = trophy.rotY;
        initialRotZ = trophy.rotZ;
        initialPose = playerPose();
    }

    @Override
    protected void init() {
        boolean poseControls = isPlayerTrophy();
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

        addRenderableWidget(Button.builder(Component.translatable("gui.trophy.reset"), button -> reset()).pos(panelLeft + 10, panelTop + sliderTop + 69).size(65, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.ok"), button -> onClose()).pos(panelLeft + 76, panelTop + sliderTop + 69).size(65, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, panelLeft, panelTop, 0.0F, 0.0F, WIDTH, panelHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(font, getTitle(), panelLeft + 75, panelTop + TITLE_ROW, TITLE_COLOR);
        graphics.centeredText(font, Component.translatable("gui.trophy.size", trophy.scale), panelLeft + 75, panelTop + SCALE_ROW + LABEL_INSET, LABEL_COLOR);
        graphics.centeredText(font, Component.translatable("gui.trophy.offset", trophy.offsetY), panelLeft + 75, panelTop + OFFSET_ROW + LABEL_INSET, LABEL_COLOR);
        if (isPlayerTrophy()) {
            graphics.centeredText(font, Component.translatable("gui.trophy.pose", playerPose().label()), panelLeft + 75, panelTop + POSE_ROW + LABEL_INSET, LABEL_COLOR);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();

        if (trophy.offsetY == initialOffsetY && trophy.scale == initialScale
                && trophy.rotX == initialRotX && trophy.rotY == initialRotY && trophy.rotZ == initialRotZ
                && playerPose() == initialPose) {
            return;
        }

        CompoundTag tag = new CompoundTag();
        tag.putDouble("OffsetY", trophy.offsetY);
        tag.putFloat("Scale", trophy.scale);
        tag.putFloat("RotX", trophy.rotX);
        tag.putFloat("RotY", trophy.rotY);
        tag.putFloat("RotZ", trophy.rotZ);
        if (isPlayerTrophy()) {
            tag.putString(TrophyPose.NBT_KEY, playerPose().name());
        }
        ClientPacketDistributor.sendToServer(new PacketUpdateTrophy(trophy.getBlockPos(), tag));
    }

    private void reset() {
        PropertiesMap defaults = defaultProperties();
        trophy.scale = defaults != null ? defaults.scale() : TrophyManagerConfig.GENERAL.defaultScale.get().floatValue();
        trophy.offsetY = defaults != null ? defaults.yOffset() : TrophyManagerConfig.GENERAL.defaultYOffset.get();
        trophy.rotX = defaults != null ? defaults.rotX() : 0.0F;
        trophy.rotY = 0.0F;
        trophy.rotZ = 0.0F;
        if (isPlayerTrophy()) {
            trophy.entity.putString(TrophyPose.NBT_KEY, TrophyPose.STANDING.name());
        }
        rebuildWidgets();
    }

    private @Nullable PropertiesMap defaultProperties() {
        if (trophy.entity == null) {
            return null;
        }
        return EntityType.byString(trophy.entity.getStringOr("entityType", ""))
                .map(type -> type.builtInRegistryHolder().getData(TrophyManager.PROPERTIES_MAP))
                .orElse(null);
    }

    private void changePose(int direction) {
        if (isPlayerTrophy()) {
            trophy.entity.putString(TrophyPose.NBT_KEY, playerPose().cycle(direction).name());
        }
    }

    private TrophyPose playerPose() {
        return trophy.entity == null ? TrophyPose.STANDING : TrophyPose.byName(trophy.entity.getStringOr(TrophyPose.NBT_KEY, ""));
    }

    private boolean isPlayerTrophy() {
        return "entity".equals(trophy.trophyType) && trophy.entity != null
                && trophy.entity.getStringOr("entityType", "").equals(ModEntities.PLAYER.getId().toString());
    }

    private void adjustScale(float d) {
        if (Minecraft.getInstance().hasShiftDown()) {
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
        if (Minecraft.getInstance().hasShiftDown()) {
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

    public static void open(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        if (level != null && level.getBlockEntity(pos) instanceof TrophyBlockEntity trophy) {
            Minecraft.getInstance().setScreen(new TrophyScreen(trophy));
        }
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
