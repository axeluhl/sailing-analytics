package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;

public class FinishPositioningListFinder extends AbstractFinishPositioningListFinder {
    public FinishPositioningListFinder(RaceLog raceLog) {
        super(raceLog, RaceLogFinishPositioningListChangedEvent.class);
    }
}
