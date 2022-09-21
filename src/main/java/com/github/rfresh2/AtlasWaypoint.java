package com.github.rfresh2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AtlasWaypoint {
    @JsonProperty("name")
    public String name;
    @JsonProperty("x")
    public String x;
    @JsonProperty("y")
    public String y;
    @JsonProperty("z")
    public String z;
    @JsonProperty("end_dimension")
    public String endDimension;
}
