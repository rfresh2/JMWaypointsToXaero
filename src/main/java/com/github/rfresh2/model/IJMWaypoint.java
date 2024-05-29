package com.github.rfresh2.model;

import java.util.List;

public interface IJMWaypoint {
    String getName();
    // position
    int getX();
    int getY();
    int getZ();
    // color
    int getR();
    int getG();
    int getB();

    boolean isEnabled();

    /**
     * 0 = overworld
     * 1 = end
     * -1 = nether
     */
    List<Integer> dimensions();

    default int getPrimaryDimension() {
        if (dimensions().contains(1)) {
            return 1;
        } else if (dimensions().contains(0)) {
            return 0;
        } else if (dimensions().contains(-1)) {
            return -1;
        } else {
            return 0; // custom dimension?
        }
    }
}
