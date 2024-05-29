package com.github.rfresh2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 1.12.2 and prior
 */
public class JMWaypointLegacy implements IJMWaypoint {
    public final String name;
    public final int x;
    public final int y;
    public final int z;
    public final int r;
    public final int g;
    public final int b;
    public final boolean enable;
    public final int[] dimensions;

    @JsonCreator
    public JMWaypointLegacy(
        @JsonProperty(value = "name", required = true)
        String name,
        @JsonProperty(value = "x", required = true)
        int x,
        @JsonProperty(value = "y", required = true)
        int y,
        @JsonProperty(value = "z", required = true)
        int z,
        @JsonProperty(value = "r", required = true)
        int r,
        @JsonProperty(value = "g", required = true)
        int g,
        @JsonProperty(value = "b", required = true)
        int b,
        @JsonProperty(value = "enable", required = true)
        boolean enable,
        @JsonProperty(value = "dimensions", required = true)
        int[] dimensions
    ) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.enable = enable;
        this.dimensions = dimensions;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public int getR() {
        return this.r;
    }

    @Override
    public int getG() {
        return this.g;
    }

    @Override
    public int getB() {
        return this.b;
    }

    @Override
    public boolean isEnabled() {
        return this.enable;
    }

    @Override
    public List<Integer> dimensions() {
        return IntStream.of(this.dimensions)
            .boxed()
            .collect(Collectors.toList());
    }
}
