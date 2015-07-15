package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.Revokable;

public interface SuppressedMarkPassingsEvent extends RaceLogEvent, Revokable {
    
    public Integer getZeroBasedIndexOfFirstSuppressedWaypoint();

}
