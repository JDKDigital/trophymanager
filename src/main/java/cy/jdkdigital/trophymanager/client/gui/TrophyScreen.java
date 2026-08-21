package cy.jdkdigital.trophymanager.client.gui;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import cy.jdkdigital.trophymanager.common.datamap.PropertiesMap;
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
    private static final int HEIGHT = 150;
    private static final Identifier GUI = Identifier.fromNamespaceAndPath(TrophyManager.MODID, "textures/gui/trophy.png");
    private final TrophyBlockEntity trophy;

    private final double initialOffsetY;
    private final float initialScale;
    private final float initialRotX;
    private final float initialRotY;
    private final float initialRotZ;

    protected TrophyScreen(BlockPos pos) {
        super(Component.translatable("gui.trophy.screen"));
        Level level = Minecraft.getInstance().level;
        trophy = (TrophyBlockEntity) level.getBlockEntity(pos);

        initialOffsetY = trophy.offsetY;
        initialScale = trophy.scale;
        initialRotX = trophy.rotX;
        initialRotY = trophy.rotY;
        initialRotZ = trophy.rotZ;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        addRenderableWidget(Button.builder(Component.literal("-"), button -> adjustScale(-1)).pos(relX + 10, relY + 10).size(20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> adjustScale(1)).pos(relX + 120, relY + 10).size(20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("-"), button -> adjustOffsetY(-1)).pos(relX + 10, relY + 35).size(20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> adjustOffsetY(1)).pos(relX + 120, relY + 35).size(20, 20).build());

        addRenderableWidget(new RotationSlider(relX + 10, relY + 58, 130, "X", trophy.rotX, v -> trophy.rotX = v));
        addRenderableWidget(new RotationSlider(relX + 10, relY + 80, 130, "Y", trophy.rotY, v -> trophy.rotY = v));
        addRenderableWidget(new RotationSlider(relX + 10, relY + 102, 130, "Z", trophy.rotZ, v -> trophy.rotZ = v));

        addRenderableWidget(Button.builder(Component.translatable("gui.trophy.reset"), button -> reset()).pos(relX + 10, relY + 125).size(65, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.ok"), button -> onClose()).pos(relX + 76, relY + 125).size(65, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0.0F, 0.0F, WIDTH, HEIGHT, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        graphics.centeredText(font, Component.translatable("gui.trophy.size", trophy.scale), relX + 75, relY + 15, 0xFFE0E0E0);
        graphics.centeredText(font, Component.translatable("gui.trophy.offset", trophy.offsetY), relX + 75, relY + 40, 0xFFE0E0E0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();

        if (trophy.offsetY == initialOffsetY && trophy.scale == initialScale
                && trophy.rotX == initialRotX && trophy.rotY == initialRotY && trophy.rotZ == initialRotZ) {
            return;
        }

        CompoundTag tag = new CompoundTag();
        tag.putDouble("OffsetY", trophy.offsetY);
        tag.putFloat("Scale", trophy.scale);
        tag.putFloat("RotX", trophy.rotX);
        tag.putFloat("RotY", trophy.rotY);
        tag.putFloat("RotZ", trophy.rotZ);
        ClientPacketDistributor.sendToServer(new PacketUpdateTrophy(trophy.getBlockPos(), tag));
    }

    private void reset() {
        PropertiesMap defaults = defaultProperties();
        trophy.scale = defaults != null ? defaults.scale() : TrophyManagerConfig.GENERAL.defaultScale.get().floatValue();
        trophy.offsetY = defaults != null ? defaults.yOffset() : TrophyManagerConfig.GENERAL.defaultYOffset.get();
        trophy.rotX = defaults != null ? defaults.rotX() : 0.0F;
        trophy.rotY = 0.0F;
        trophy.rotZ = 0.0F;
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
        Minecraft.getInstance().setScreen(new TrophyScreen(pos));
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
