package ch.hevs.cloudio.cloud.restapi.endpoint.history

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Fill values for missing values from time-series database if resampling is active.", enumAsRef = true)
enum class FillValue(val id: String) {

    @Schema(description = "Reports no timestamp and no value for time intervals with no data.")
    NONE("none"),

    @Schema(description = "Reports null for time intervals with no data but returns a timestamp.")
    NULL("null"),

    @Schema(description = "Fills in 0 for missing values.")
    ZERO("0"),

    @Schema(description = "Reports the value from the previous time interval for time intervals with no data.")
    PREVIOUS("previous"),

    @Schema(description = "Reports the results of linear interpolation for time intervals with no data.")
    LINEAR("linear")
}
