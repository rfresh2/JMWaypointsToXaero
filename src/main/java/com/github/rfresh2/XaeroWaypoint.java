package com.github.rfresh2;

import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class XaeroWaypoint {
    String name;
    String initials;
    int x;
    int y;
    int z;
    /**
     * Color index
     * <p>
     * 0: Black
     */
    int color;
    boolean disabled;
    int type;
    String set;
    boolean rotate_on_tp;
    int tp_yaw;
    int visibility_type;

    // not in output file
    int dimension;

    public XaeroWaypoint(String name, String initials, int x, int y, int z, int color, boolean disabled, int type, String set, boolean rotate_on_tp, int tp_yaw, int visibility_type, int dimension) {
        this.name = name;
        this.initials = initials;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        this.disabled = disabled;
        this.type = type;
        this.set = set;
        this.rotate_on_tp = rotate_on_tp;
        this.tp_yaw = tp_yaw;
        this.visibility_type = visibility_type;
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return asList("waypoint", name, initials, x, y, z, color, disabled, type, set, rotate_on_tp, tp_yaw, visibility_type).stream()
                .map(Objects::toString)
                .collect(Collectors.joining(":"));
    }
}

