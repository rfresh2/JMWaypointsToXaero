package com.github.rfresh2;


import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;


public class AtlasWaypointsToXaero {

    private final ObjectMapper objectMapper;
    static final Random random = new Random();


    public AtlasWaypointsToXaero() {
        this.objectMapper = JsonMapper.builder()
                .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false)
                .build();
    }

    public static void main(final String[] args) throws IOException {
        final AtlasWaypointsToXaero atlasWaypointsToXaero = new AtlasWaypointsToXaero();
        final String rawJson = atlasWaypointsToXaero.getAtlasApiResponse();
        List<AtlasWaypoint> atlasWaypoints = atlasWaypointsToXaero.parseAtlasWaypoints(rawJson);
        IntStream.of(0, 1).boxed().forEach(dim -> {
            List<XaeroWaypoint> xaeroWaypoints = atlasWaypoints.stream()
                    .filter(wp -> Integer.parseInt(wp.endDimension) == dim)
                    .map(AtlasWaypointsToXaero::convertAtlasToXaero)
                    .collect(Collectors.toList());
            String out = atlasWaypointsToXaero.writeDimensionWaypoints(dim, xaeroWaypoints);
            System.out.println("DIM: " + dim + " ===============================================================");
            System.out.println(out);
        });
    }

    private static XaeroWaypoint convertAtlasToXaero(AtlasWaypoint atlasWaypoint) {
        return new XaeroWaypoint(
                atlasWaypoint.name,
                atlasWaypoint.name.substring(0, Math.min(atlasWaypoint.name.length(), 2)),
                Integer.parseInt(atlasWaypoint.x),
                isNull(atlasWaypoint.y) ? 64 : Integer.parseInt(atlasWaypoint.y),
                Integer.parseInt(atlasWaypoint.z),
                random.nextInt(16),
                true,
                0,
                "atlas",
                false,
                0,
                0,
                Integer.parseInt(atlasWaypoint.endDimension));
    }

    private String getAtlasApiResponse() throws IOException {
        return Request.get("https://2b2tAtlas.com/api/locations.php")
                    .execute().returnContent().toString();
    }

    public List<AtlasWaypoint> parseAtlasWaypoints(final String rawJson) throws IOException {
        return Arrays.asList(this.objectMapper.readValue(rawJson, AtlasWaypoint[].class));
    }

    private String writeDimensionWaypoints(int dimension, List<XaeroWaypoint> waypoints) {
        return waypoints.stream()
                .filter(wp -> wp.dimension == dimension)
                .map(XaeroWaypoint::toString)
                .collect(Collectors.joining("\n"));
    }
}
