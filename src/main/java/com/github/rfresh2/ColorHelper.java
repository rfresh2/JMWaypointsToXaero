package com.github.rfresh2;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ColorHelper {
    /**
     * Color indexes:
     * 0 = black
     * 1 = dark blue
     * 2 = dark green
     * 3 = dark aqua
     * 4 = dark red
     * 5 = dark purple
     * 6 = gold
     * 7 = gray
     * 8 = dark gray
     * 9 = blue
     * 10 = green
     * 11 = aqua
     * 12 = red
     * 13 = light purple
     * 14 = yellow
     * 15 = white
     */
    public static List<Color> xaeroColors = Stream.of(-16777216, -16777046, -16733696, -16733526, -5636096, -5635926, -22016, -5592406, -11184811, -11184641, -11141291, -11141121, -65536, -43521, -171, -1)
        .map(Color::new)
        .collect(Collectors.toList());

    public static int nearestXaeroColorIndex(Color jmColor) {
        int minIndex = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < xaeroColors.size(); i++) {
            Color xaeroColor = xaeroColors.get(i);
            double d = colorDistance(xaeroColor, jmColor);
            if (d < min) {
                minIndex = i;
                min = d;
            }
        }
        return minIndex;
    }

    public static double colorDistance(Color c1, Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int rmean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
    }
}
