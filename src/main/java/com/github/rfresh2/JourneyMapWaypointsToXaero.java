package com.github.rfresh2;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.github.rfresh2.model.IJMWaypoint;
import com.github.rfresh2.model.JMWaypointLegacy;
import com.github.rfresh2.model.JMWaypointModern;
import com.github.rfresh2.model.XaeroWaypoint;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JourneyMapWaypointsToXaero {
    static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true);
    static {
        // jackson will otherwise convert int values to String
        // legacy JM WP dimensions are int[]
        // modern JM WP dimensions are String[]
        // we want to try modern, and if it fails, try legacy
        objectMapper.coercionConfigFor(LogicalType.Textual).setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
    }
    static final Logger LOG = LoggerFactory.getLogger("JMWaypointsToXaero");

    public static void main(final String[] args) {
        if (args.length < 2) {
            LOG.error("Usage: <input folder> <output folder>");
            System.exit(1);
        }
        String input = args[0];
        String output = args[1];
        Path folderIn = new File(String.format("%s/waypoints/", input)).toPath();
        if (Files.notExists(folderIn)) {
            LOG.error("Input folder does not exist: {}", folderIn);
            System.exit(1);
        }
        LOG.info("Reading JM waypoints from path: {}", folderIn.toAbsolutePath());
        List<XaeroWaypoint> xaeroWaypoints = convertWaypoints(folderIn);
        xaeroWaypoints.stream()
            .map(wp -> wp.dimension)
            .distinct()
            .forEach(dim -> writeDimensionWaypoints(output, dim, xaeroWaypoints));
    }

    private static List<XaeroWaypoint> convertWaypoints(Path inputFolder) {
        File inputFolderFile = inputFolder.toFile();
        List<File> files = Arrays.asList(Objects.requireNonNull(inputFolderFile.listFiles()));

        for (File file : files) {
            if (file.getName().equals("WaypointData.dat")) {
                LOG.info("Found JourneyMap NBT waypoint data file: {}", file.getAbsolutePath());
                try {
                    List<JMWaypointModern> jmWaypoints = readNbtWaypoints(file.toPath());
                    return jmWaypoints.stream()
                        .peek(wp -> LOG.info("Found {} JM waypoint: {} [{}, {}, {}]",
                            "nbt",
                            wp.getName(),
                            wp.getX(),
                            wp.getY(),
                            wp.getZ()))
                        .map(JourneyMapWaypointsToXaero::convertWaypoint)
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    LOG.error("Error reading WaypointData.dat file: {}", file.getAbsolutePath(), e);
                    throw new RuntimeException(e);
                }
            }
        }

        return files.stream()
            .map(JourneyMapWaypointsToXaero::parseJourneyMapWaypointFile)
            .filter(Objects::nonNull)
            .peek(wp -> LOG.info("Found {} JM waypoint: {} [{}, {}, {}]",
                                 wp instanceof JMWaypointModern ? "modern" : "legacy",
                                 wp.getName(),
                                 wp.getX(),
                                 wp.getY(),
                                 wp.getZ()))
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
            LOG.info("Writing waypoints for dimension: " + dimension + " to: " + fileOut);
            Files.write(fileOut, outputFileContents.toString().getBytes(StandardCharsets.UTF_8), openOption);
        } catch (IOException e) {
            LOG.error("Failed writing Xaero waypoint outputs", e);
        }
    }

    public static IJMWaypoint parseJourneyMapWaypointFile(final File file) {
        try {
            return objectMapper.readValue(file, JMWaypointModern.class);
        } catch (IOException e) {
            // try legacy next
            LOG.debug("Error parsing modern JM waypoint", e);
        }
        try {
            return objectMapper.readValue(file, JMWaypointLegacy.class);
        } catch (IOException e) {
            LOG.debug("Error parsing legacy JM waypoint", e);
        }
        LOG.error("Unable to read JM waypoint from file: {}", file.getName());
        return null;
    }

    public static XaeroWaypoint convertWaypoint(final IJMWaypoint jmWaypoint) {
        int dimension = jmWaypoint.getPrimaryDimension();
        return new XaeroWaypoint(
                jmWaypoint.getName(),
                jmWaypoint.getName().substring(0, Math.min(jmWaypoint.getName().length(), 2)),
                (dimension == -1 ? jmWaypoint.getX() / 8 : jmWaypoint.getX()), // jm stores all wp in ow coords
                jmWaypoint.getY(),
                (dimension == -1 ? jmWaypoint.getZ() / 8 : jmWaypoint.getZ()),
                ColorHelper.nearestXaeroColorIndex(new Color(jmWaypoint.getR(), jmWaypoint.getG(), jmWaypoint.getB())),
                !jmWaypoint.isEnabled(),
                0,
                "gui.xaero_default",
                false,
                0,
                0,
                dimension
        );
    }

    public static List<JMWaypointModern> readNbtWaypoints(Path nbtSrc) throws Exception {
        List<JMWaypointModern> waypoints = new ArrayList<>();
        CompoundTag tag = (CompoundTag) NBTIO.reader().named().read(nbtSrc, false);
        CompoundTag waypointsTag = tag.getCompoundTag("waypoints");
        for (Tag waypointTagEntry : waypointsTag.values()) {
            CompoundTag e = (CompoundTag) waypointTagEntry;
            String waypointName = e.getString("name");
            CompoundTag posTag = e.getCompoundTag("pos");
            int x = posTag.getInt("x");
            int y = posTag.getInt("y");
            int z = posTag.getInt("z");
            String dimension = posTag.getString("dimension");
            int color = e.getInt("color");
            int r = color >> 16 & 0xFF;
            int g = color >> 8 & 0xFF;
            int b = color & 0xFF;
            JMWaypointModern jmWaypointJsonModern = new JMWaypointModern(waypointName, x, y, z, r, g, b, true, new String[]{dimension});
            waypoints.add(jmWaypointJsonModern);
        }
        return waypoints;
    }
}
