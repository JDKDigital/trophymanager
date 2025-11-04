package cy.jdkdigital.trophymanager.network;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.client.gui.TrophyScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketOpenGui(BlockPos pos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<PacketOpenGui> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TrophyManager.MODID, "open_gui"));

    public static final StreamCodec<ByteBuf, PacketOpenGui> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BlockPos.CODEC),
            PacketOpenGui::pos,
            PacketOpenGui::new
    );

    public static void clientHandle(final PacketOpenGui data, final IPayloadContext context) {
        TrophyScreen.open(data.pos());
    }

    public static void serverHandle(final PacketOpenGui data, final IPayloadContext context) {
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
