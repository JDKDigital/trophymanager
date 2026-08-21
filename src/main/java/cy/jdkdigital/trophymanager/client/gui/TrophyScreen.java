package cy.jdkdigital.trophymanager.client.gui;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import cy.jdkdigital.trophymanager.network.PacketUpdateTrophy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class TrophyScreen extends Screen
{
    private static final int WIDTH = 150;
    private static final int HEIGHT = 150;
    private static final Identifier GUI = Identifier.fromNamespaceAndPath(TrophyManager.MODID, "textures/gui/trophy.png");
    private final TrophyBlockEntity trophy;

    protected TrophyScreen(BlockPos pos) {
        super(Component.translatable("gui.trophy.screen"));
        Level level = Minecraft.getInstance().level;
        trophy = (TrophyBlockEntity) level.getBlockEntity(pos);
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        addRenderableWidget(Button.builder(Component.literal("-"), button -> adjustScale(-1)).pos(relX + 10, relY + 10).size(20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> adjustScale(1)).pos(relX + 120, relY + 10).size(20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("-"), button -> adjustOffsetY(-1)).pos(relX + 10, relY + 35).size(20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> adjustOffsetY(1)).pos(relX + 120, relY + 35).size(20, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> close()).pos(relX + 10, relY + 60).size(65, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.apply"), button -> save(this)).pos(relX + 76, relY + 60).size(65, 20).build());
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
        graphics.centeredText(font, "" + trophy.scale, relX + 75, relY + 15, 0xFFE0E0E0);
        graphics.centeredText(font, "" + trophy.offsetY, relX + 75, relY + 40, 0xFFE0E0E0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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

    public static void save(TrophyScreen screen) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("OffsetY", screen.trophy.offsetY);
        tag.putFloat("Scale", screen.trophy.scale);
        ClientPacketDistributor.sendToServer(new PacketUpdateTrophy(screen.trophy.getBlockPos(), tag));
        close();
    }

    public static void close() {
        Minecraft.getInstance().setScreen(null);
    }
}
