package dev.IlikeHello47.stormchasers_dream.weather;

import dev.IlikeHello47.stormchasers_dream.network.SyncStormCellsPayload;
import dev.IlikeHello47.stormchasers_dream.weather.impl.StormCell;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StormManager {

    private static final List<StormCell> SERVER_CELLS = new CopyOnWriteArrayList<>();
    private static final List<StormCell> CLIENT_CELLS = new CopyOnWriteArrayList<>();

    public static List<StormCell> getActiveCells(boolean isClient) {
        return isClient ? CLIENT_CELLS : SERVER_CELLS;
    }

    public static void tickServer(ServerLevel level) {
        List<StormCell> toRemove = new ArrayList<>();

        for (StormCell cell : SERVER_CELLS) {
            cell.center = cell.center.offset(1, 0, 0);

            cell.radius -= 0.01;
            if (cell.radius <= 10) {
                toRemove.add(cell);
            }
        }

        if (!toRemove.isEmpty()) {
            SERVER_CELLS.removeAll(toRemove);
            syncToAllPlayers(level);
        }

        if (SERVER_CELLS.size() < 3 && level.random.nextDouble() < 0.0005) {
            if (!level.players().isEmpty()) {
                ServerPlayer player = level.players().get(level.random.nextInt(level.players().size()));

                int offsetX = (level.random.nextBoolean() ? 1 : -1) * (150 + level.random.nextInt(100));
                int offsetZ = (level.random.nextBoolean() ? 1 : -1) * (150 + level.random.nextInt(100));
                BlockPos spawnPos = player.blockPosition().offset(offsetX, 0, offsetZ);

                boolean isSupercell = level.random.nextDouble() < 0.15;
                double radius = 100.0 + level.random.nextDouble() * 150.0;

                SERVER_CELLS.add(new StormCell(spawnPos, radius, isSupercell));
                syncToAllPlayers(level);
                System.out.println((isSupercell ? "[Supercell]" : "[Stormcell]") + " spawned at: " + spawnPos);
            }
        }
    }

    public static StormCell getCellAt(BlockPos pos, boolean isClient) {
        List<StormCell> cells = getActiveCells(isClient);
        for (StormCell cell : cells) {
            if (cell.isInside(pos)) {
                return cell;
            }
        }
        return null;
    }

    public static void updateClientCells(List<StormCell> newCells) {
        CLIENT_CELLS.clear();
        CLIENT_CELLS.addAll(newCells);
    }

    private static void syncToAllPlayers(ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, new SyncStormCellsPayload(SERVER_CELLS));
    }
}
