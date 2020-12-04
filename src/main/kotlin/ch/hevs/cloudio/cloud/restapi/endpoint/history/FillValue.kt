package ch.hevs.cloudio.cloud.restapi.endpoint.history

enum class FillValue(val id: String) {

    // Reports no timestamp and no value for time intervals with no data.
    NONE("none"),

    // Reports null for time intervals with no data but returns a timestamp.
    NULL("null"),

    // Fills in 0 for missing values.
    ZERO("0"),

    // Reports the value from the previous time interval for time intervals with no data.
    PREVIOUS("previous"),

    // Reports the results of linear interpolation for time intervals with no data.
    LINEAR("linear")
}
