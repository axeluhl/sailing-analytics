package com.sap.sse.gwt.client.controls.slider;

enum TimeUnits {
    MILLISECOND(1l, 1, 2, 5, 10, 20, 25, 50, 100, 200, 500), 
    SECOND(1000l, 1, 2, 5, 10, 15, 30),
    MINUTE(60 * 1000l, 1, 2, 5, 10, 15, 30),
    HOUR(60 * 60 * 1000l, 1, 2, 3, 4, 6, 8, 12),
    DAY(24 * 3600000l, 1, 2), 
    WEEK(7 * 24 * 3600000l, 1, 2), 
    MONTH(30 * 24 * 3600000l, 1, 2, 3, 4, 6),
    YEAR(31556952000l);

    int[] allowedMultiples;

    long unitInMs;

    TimeUnits(long unitInMs, int... allowedMultiples) {
        this.unitInMs = unitInMs;
        this.allowedMultiples = allowedMultiples;
    }
}