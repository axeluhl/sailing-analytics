package com.sap.sailing.domain.base;

import java.io.Serializable;

public interface Leg extends Serializable {
    Waypoint getFrom();

    Waypoint getTo();
}
