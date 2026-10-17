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
public class ViewportRendererMixin {

    @Inject(method = "getBrightnessDependentFogColor", at = @At("RETURN"), cancellable = true)
    private void onGetFogColor(Vec3 biomeFogColor, float daylight, CallbackInfoReturnable<Vec3> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            BlockPos pos = mc.player.blockPosition();
            StormCell cell = StormManager.getCellAt(pos, true);

            if (cell != null && cell.isSupercell) {
                Vec3 originalColor = cir.getReturnValue();

                double greenTargetR = 0.15;
                double greenTargetG = 0.28;
                double greenTargetB = 0.22;

                Vec3 supercellColor = new Vec3(
                        originalColor.x * 0.4 + greenTargetR * 0.6,
                        originalColor.y * 0.5 + greenTargetG * 0.5,
                        originalColor.z * 0.4 + greenTargetB * 0.6
                );

                cir.setReturnValue(supercellColor);
            }
        }
    }
}
