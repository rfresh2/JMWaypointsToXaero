package com.github.rfresh2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class JourneyMapWaypointsToXaero {
    static final ObjectMapper objectMapper = new ObjectMapper();
    static final Random random = new Random();
    static final List<Integer> possibleDimensions = asList(-1, 0, 1);

    public static void main(final String[] args) {
        if (args.length < 2) {
            System.err.println("usage: <input folder> <output folder>");
            System.exit(1);
        }
        String input = args[0];
        String output = args[1];

        Path folderIn = new File(String.format("%s/waypoints/", input)).toPath();
        List<XaeroWaypoint> xaeroWaypoints = convertWaypoints(folderIn);
        possibleDimensions.forEach(dim -> writeDimensionWaypoints(output, dim, xaeroWaypoints));
    }

    private static List<XaeroWaypoint> convertWaypoints(Path inputFolder) {
        return Arrays.stream(Objects.requireNonNull(inputFolder.toFile().listFiles()))
                .map(JourneyMapWaypointsToXaero::parseJourneyMapWaypointFile)
                .filter(Objects::nonNull)
                .map(JourneyMapWaypointsToXaero::convertWaypoint)
                .collect(Collectors.toList());
    }

    private static void writeDimensionWaypoints(String outputDir, int dimension, List<XaeroWaypoint> waypoints) {
        Path folderOut = new File(String.format("%s/%s/", outputDir, "dim%" + dimension)).toPath();
        Path fileOut = new File(folderOut + "/mw$default_1.txt").toPath();

        File folderCheck = new File(String.valueOf(folderOut.toFile()));
        File parentCheck = new File(String.valueOf(folderOut.toFile().getParentFile()));
        if (!parentCheck.exists()) {
            parentCheck.mkdir();
            folderCheck.mkdir();
        } else if (!folderCheck.exists()) {
            folderCheck.mkdir();
        }
        File outFileCheck = fileOut.toFile();
        OpenOption openOption = StandardOpenOption.CREATE;
        final StringBuilder outputFileContents = new StringBuilder();
        if (outFileCheck.exists()) {
            // todo: make some option to append instead
            outFileCheck.delete();
        }
        outputFileContents.append("#\n");
        outputFileContents.append("#waypoint:name:initials:x:y:z:color:disabled:type:set:rotate_on_tp:tp_yaw:visibility_type\n");
        outputFileContents.append("#\n");
        waypoints.stream()
                .filter(wp -> wp.dimension == dimension)
                .forEach(wp -> outputFileContents.append(wp).append("\n"));
        try {
            System.out.println("Writing waypoints for dimension: " + dimension + " to: " + fileOut);
            Files.write(fileOut, outputFileContents.toString().getBytes(StandardCharsets.UTF_8), openOption);
        } catch (IOException e) {
            System.out.println("Failed writing Xaero waypoint outputs");
            e.printStackTrace();
        }
    }

    public static JourneyMapWaypoint parseJourneyMapWaypointFile(final File file) {
        try {
            return objectMapper.readValue(file, JourneyMapWaypoint.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static XaeroWaypoint convertWaypoint(final JourneyMapWaypoint journeyMapWaypoint) {
        System.out.println("Converting waypoint: " + journeyMapWaypoint.name);
        int dimension = determineDimension(journeyMapWaypoint);
        return new XaeroWaypoint(
                journeyMapWaypoint.name,
                journeyMapWaypoint.name.substring(0, Math.min(journeyMapWaypoint.name.length(), 2)),
                (dimension == -1 ? journeyMapWaypoint.x / 8 : journeyMapWaypoint.x), // jm stores all wp in ow coords
                journeyMapWaypoint.y,
                (dimension == -1 ? journeyMapWaypoint.z / 8 : journeyMapWaypoint.z),
                random.nextInt(16), // todo: convert JM rgb to some equivalent Xaero color
                !journeyMapWaypoint.enable,
                0,
                "gui.xaero_default",
                false,
                0,
                0,
                dimension
        );
    }

    public static int determineDimension(final JourneyMapWaypoint journeyMapWaypoint) {
        if (journeyMapWaypoint.dimensions.contains("minecraft:the_end")) {
            return 1;
        }
        // prefer defining waypoints as ow instead of nether, even if journeymap wp is visible in nether
        if (journeyMapWaypoint.dimensions.contains("minecraft:overworld")) {
            return 0;
        }
        if (journeyMapWaypoint.dimensions.contains("minecraft:the_nether")) {
            return -1;
        }
        return 0; //default
    }

    public static class XaeroWaypoint {
        String name;
        String initials;
        int x;
        int y;
        int z;
        /**
         * Color index
         *
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

    public static class JourneyMapWaypoint {
        @JsonProperty("id")
        String id;
        @JsonProperty("name")
        String name;
        @JsonProperty("icon")
        String icon;
        @JsonProperty("colorizedIcon")
        String colorizedIcon;
        // always stored as overworld coords
        @JsonProperty("x")
        int x;
        @JsonProperty("y")
        int y;
        @JsonProperty("z")
        int z;
        @JsonProperty("r")
        int r;
        @JsonProperty("g")
        int g;
        @JsonProperty("b")
        int b;
        @JsonProperty("enable")
        boolean enable;
        @JsonProperty("type")
        String type;
        @JsonProperty("origin")
        String origin;
        @JsonProperty("dimensions")
        List<String> dimensions;
        @JsonProperty("persistent")
        boolean persistent;
        @JsonProperty("showDeviation")
        boolean showDeviation;
        @JsonProperty("iconColor")
        int iconColor;
        @JsonProperty("customIconColor")
        boolean customIconColor;

        public JourneyMapWaypoint() {}

        public JourneyMapWaypoint(String id, String name, String icon, int x, int y, int z, int r, int g, int b, boolean enable, String type, String origin, List<String> dimensions, boolean persistent) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.x = x;
            this.y = y;
            this.z = z;
            this.r = r;
            this.g = g;
            this.b = b;
            this.enable = enable;
            this.type = type;
            this.origin = origin;
            this.dimensions = dimensions;
            this.persistent = persistent;
        }
    }
}
