package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;

public class ConfirmedFinishPositioningListFinder extends AbstractFinishPositioningListFinder {
    public ConfirmedFinishPositioningListFinder(RaceLog raceLog) {
        super(raceLog, RaceLogFinishPositioningConfirmedEvent.class);
    }
}
