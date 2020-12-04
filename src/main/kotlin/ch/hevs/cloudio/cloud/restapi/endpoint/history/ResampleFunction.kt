package ch.hevs.cloudio.cloud.restapi.endpoint.history

enum class ResampleFunction {
    // Returns the number of non-null field values.
    COUNT,

    // Returns the list of unique field values.
    DISTINCT,

    // Returns the area under the curve for subsequent field values.
    INTEGRAL,

    // Returns the arithmetic mean (average) of field values.
    MEAN,

    // Returns the middle value from a sorted list of field values.
    MEDIAN,

    // Returns the most frequent value in a list of field values.
    MODE,

    // Returns the difference between the minimum and maximum field values.
    SPREAD,

    // Returns the standard deviation of field values.
    STDDEV,

    // Returns the sum of field values.
    SUM,

    // Returns the field value with the oldest timestamp.
    FIRST,

    // Returns the field value with the most recent timestamp.
    LAST,

    // Returns the greatest field value.
    MAX,

    // Returns the lowest field value.
    MIN
}