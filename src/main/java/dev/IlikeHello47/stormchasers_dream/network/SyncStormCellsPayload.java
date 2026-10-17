package dev.IlikeHello47.stormchasers_dream.network;

import dev.IlikeHello47.stormchasers_dream.StormchasersDream;
import dev.IlikeHello47.stormchasers_dream.weather.impl.StormCell;
import dev.IlikeHello47.stormchasers_dream.weather.StormManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncStormCellsPayload(List<StormCell> cells) implements CustomPacketPayload {

    public static final Type<SyncStormCellsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(StormchasersDream.MODID, "sync_storms"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStormCellsPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.cells().size());
                for (StormCell cell : payload.cells()) {
                    buf.writeBlockPos(cell.center);
                    buf.writeDouble(cell.radius);
                    buf.writeBoolean(cell.isSupercell);
                }
            },
            buf -> {
                int size = buf.readInt();
                List<StormCell> cells = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    BlockPos center = buf.readBlockPos();
                    double radius = buf.readDouble();
                    boolean isSupercell = buf.readBoolean();
                    cells.add(new StormCell(center, radius, isSupercell));
                }
                return new SyncStormCellsPayload(cells);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final SyncStormCellsPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            StormManager.updateClientCells(payload.cells());
        });
    }
}
