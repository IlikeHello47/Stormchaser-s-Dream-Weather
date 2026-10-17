package dev.IlikeHello47.stormchasers_dream.mixin.client;

import dev.IlikeHello47.stormchasers_dream.weather.StormManager;
import dev.IlikeHello47.stormchasers_dream.weather.impl.StormCell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow private ClientLevel level;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevel(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && this.level != null) {
            BlockPos playerPos = mc.player.blockPosition();
            StormCell cell = StormManager.getCellAt(playerPos, true);

            if (cell != null) {
                this.level.rainLevel = cell.isSupercell ? 1.0f : 0.6f;
                this.level.oRainLevel = this.level.rainLevel;

                this.level.thunderLevel = cell.isSupercell ? 1.0f : 0.3f;
                this.level.oThunderLevel = this.level.thunderLevel;
            }
        }
    }
}
