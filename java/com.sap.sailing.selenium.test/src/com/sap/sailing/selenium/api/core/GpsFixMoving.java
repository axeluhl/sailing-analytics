package com.sap.sailing.selenium.api.core;

public class GpsFixMoving extends GpsFix {

    private static final String ATTRIBUTE_SPEED = "speed";
    private static final String ATTRIBUTE_COURSE = "course";

    public static GpsFixMoving createFix(final Double longitude, final Double latitude, final Long timestamp,
            final Double speed, final Double course) {
        return new GpsFixMoving(longitude, latitude, timestamp, speed, course);
    }

    GpsFixMoving(final Double longitude, final Double latitude, final Long timestamp, final Double speed,
            final Double course) {
        super(longitude, latitude, timestamp);
        getJson().put(ATTRIBUTE_SPEED, speed);
        getJson().put(ATTRIBUTE_COURSE, course);
    }

    public final Double getSpeed() {
        return get(ATTRIBUTE_SPEED);
    }

    public final Double getCourse() {
        return get(ATTRIBUTE_COURSE);
    }

}
