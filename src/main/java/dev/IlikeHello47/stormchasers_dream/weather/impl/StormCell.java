package dev.IlikeHello47.stormchasers_dream.weather.impl;

import net.minecraft.core.BlockPos;

public class StormCell {
    public BlockPos center;
    public double radius;
    public boolean isSupercell;

    public StormCell(BlockPos center, double radius, boolean isSupercell) {
        this.center = center;
        this.radius = radius;
        this.isSupercell = isSupercell;
    }

    public boolean isInside(BlockPos pos) {
        return this.center.distSqr(pos) <= (radius * radius);
    }
}
