package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;

/**
 * Represents the time point, position and the wind course measured during analysis of a {@link ManeuverSpot}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
class WindMeasurement {

    private final TimePoint timePoint;
    private final Position position;
    private final Bearing windCourse;

    /**
     * Constructs a wind measurement record.
     * 
     * @param timePoint
     *            The time point of wind measurement
     * @param position
     *            The position of wind measurement
     * @param windCourse
     *            The course of the wind, which may be {@code null}
     */
    public WindMeasurement(TimePoint timePoint, Position position, Bearing windCourse) {
        this.timePoint = timePoint;
        this.position = position;
        this.windCourse = windCourse;
    }

    /**
     * The time point when the wind was measured
     */
    public TimePoint getTimePoint() {
        return timePoint;
    }

    /**
     * The position where the wind was measured.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * The wind course measured during maneuver analysis.
     * 
     * @return {@code null} if the wind was not available, or not necessary for analysis, otherwise the wind course.
     */
    public Bearing getWindCourse() {
        return windCourse;
    }
}
