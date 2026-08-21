package cy.jdkdigital.trophymanager.network;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpdateTrophy(BlockPos pos, CompoundTag tag) implements CustomPacketPayload
{
    public static final Type<PacketUpdateTrophy> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TrophyManager.MODID, "update_trophy"));

    public static final StreamCodec<ByteBuf, PacketUpdateTrophy> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BlockPos.CODEC),
            PacketUpdateTrophy::pos,
            ByteBufCodecs.fromCodec(CompoundTag.CODEC),
            PacketUpdateTrophy::tag,
            PacketUpdateTrophy::new
    );

    public static void clientHandle(final PacketUpdateTrophy data, final IPayloadContext context) {

    }

    public static void serverHandle(final PacketUpdateTrophy data, final IPayloadContext context) {
        if (context.player().level().getBlockEntity(data.pos()) instanceof TrophyBlockEntity trophyBlockEntity) {
            trophyBlockEntity.offsetY = Math.min(data.tag().getDoubleOr("OffsetY", 0.0D), TrophyManagerConfig.GENERAL.maxYOffset.get());
            trophyBlockEntity.scale = (float) Math.min(data.tag().getFloatOr("Scale", 1.0F), TrophyManagerConfig.GENERAL.maxSize.get());
            trophyBlockEntity.rotX = wrapDegrees(data.tag().getFloatOr("RotX", 0.0F));
            trophyBlockEntity.rotY = wrapDegrees(data.tag().getFloatOr("RotY", 0.0F));
            trophyBlockEntity.rotZ = wrapDegrees(data.tag().getFloatOr("RotZ", 0.0F));
            if (data.tag().contains("PoseType") && trophyBlockEntity.entity != null) {
                trophyBlockEntity.entity.putString("PoseType", data.tag().getStringOr("PoseType", ""));
            }
            trophyBlockEntity.getCachedEntity();
            trophyBlockEntity.setChanged();
            context.player().level().sendBlockUpdated(data.pos(), trophyBlockEntity.getBlockState(), trophyBlockEntity.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static float wrapDegrees(float degrees) {
        if (!Float.isFinite(degrees)) {
            return 0.0F;
        }
        return ((degrees % 360.0F) + 360.0F) % 360.0F;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
