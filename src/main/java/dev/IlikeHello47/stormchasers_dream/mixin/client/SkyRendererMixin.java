package dev.IlikeHello47.stormchasers_dream.mixin.client;

import dev.IlikeHello47.stormchasers_dream.weather.StormManager;
import dev.IlikeHello47.stormchasers_dream.weather.impl.StormCell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.OverworldEffects.class)
public class SkyRendererMixin {

    @Inject(method = "getBrightnessDependentFogColor", at = @At("RETURN"), cancellable = true)
    private void onGetFogColor(Vec3 biomeFogColor, float daylight, CallbackInfoReturnable<Vec3> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        BlockPos pos = mc.player.blockPosition();
        StormCell cell = StormManager.getCellAt(pos, true);

        if (cell == null) return;

        double distanceSqr = cell.center.distSqr(pos);
        double maxRadiusSqr = cell.radius * cell.radius;

        if (distanceSqr >= maxRadiusSqr) return;

        Vec3 vanillaColor = cir.getReturnValue();

        double distance = Math.sqrt(distanceSqr);
        double intensity = 1.0 - (distance / cell.radius);
        intensity = Math.clamp(intensity, 0.0, 1.0);

        if (intensity < 0.02) return;

        double targetR, targetG, targetB;

        if (cell.isSupercell) {
            targetR = 0.08 * (1.0 - intensity * 0.3);
            targetG = 0.18 * (1.0 + intensity * 0.2);
            targetB = 0.14 * (1.0 - intensity * 0.4);
        } else {
            targetR = 0.12;
            targetG = 0.14;
            targetB = 0.18;
        }

        Vec3 realLifeStormColor = new Vec3(
                vanillaColor.x * (1.0 - intensity) + targetR * intensity,
                vanillaColor.y * (1.0 - intensity) + targetG * intensity,
                vanillaColor.z * (1.0 - intensity) + targetB * intensity
        );

        cir.setReturnValue(realLifeStormColor);
    }
}
