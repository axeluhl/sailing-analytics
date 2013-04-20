package com.sap.sailing.domain.racelog;


public interface RaceLogGatelineOpeneningTimeEvent extends RaceLogEvent {
    long getGateLineOpeningTime();
}
