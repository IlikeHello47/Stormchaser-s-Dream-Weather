package dev.IlikeHello47.stormchasers_dream.mixin;

import dev.IlikeHello47.stormchasers_dream.weather.StormManager;
import dev.IlikeHello47.stormchasers_dream.weather.impl.StormCell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onServerTick(CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;

        if (level.isClientSide()) return;

        for (StormCell cell : StormManager.getActiveCells(false)) {

            double strikeChance = cell.isSupercell ? 0.2 : 0.02;

            if (level.random.nextDouble() < strikeChance) {
                double angle = level.random.nextDouble() * 2 * Math.PI;
                double distance = level.random.nextDouble() * cell.radius;

                int x = cell.center.getX() + (int)(Math.cos(angle) * distance);
                int z = cell.center.getZ() + (int)(Math.sin(angle) * distance);

                BlockPos strikePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));

                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(strikePos.getX(), strikePos.getY(), strikePos.getZ());

                    if (cell.isSupercell) {
                        level.explode(null, strikePos.getX(), strikePos.getY(), strikePos.getZ(), 2.0f, true, ServerLevel.ExplosionInteraction.BLOCK);
                    }

                    level.addFreshEntity(lightning);
                }
            }

            if (cell.isSupercell && level.getGameTime() % 5 == 0) {
                level.players().stream()
                        .filter(player -> cell.isInside(player.blockPosition()))
                        .forEach(player -> {
                            double dx = cell.center.getX() - player.getX();
                            double dz = cell.center.getZ() - player.getZ();
                            double dist = Math.sqrt(dx * dx + dz * dz);
                            if (dist > 5) {
                                player.push(dx / dist * 0.1, 0.02, dz / dist * 0.1);
                                player.hurtMarked = true;
                            }
                        });
            }
        }
    }
}
