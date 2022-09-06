package ch.hevs.cloudio.cloud.restapi.endpoint.history

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Function used to resample raw data in time-series database.", enumAsRef = true)
enum class ResampleFunction {
    @Schema(description = "Returns the number of non-null field values.")
    COUNT,

    @Schema(description = "Returns the list of unique field values.")
    DISTINCT,

    @Schema(description = "Returns the area under the curve for subsequent field values.")
    INTEGRAL,

    @Schema(description = "Returns the arithmetic mean (average) of field values.")
    MEAN,

    @Schema(description = "Returns the middle value from a sorted list of field values.")
    MEDIAN,

    @Schema(description = "Returns the most frequent value in a list of field values.")
    MODE,

    @Schema(description = "Returns the difference between the minimum and maximum field values.")
    SPREAD,

    @Schema(description = "Returns the standard deviation of field values.")
    STDDEV,

    @Schema(description = "Returns the sum of field values.")
    SUM,

    @Schema(description = "Returns the field value with the oldest timestamp.")
    FIRST,

    @Schema(description = "Returns the field value with the most recent timestamp.")
    LAST,

    @Schema(description = "Returns the greatest field value.")
    MAX,

    @Schema(description = "Returns the lowest field value.")
    MIN
}