package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.common.racelog.Flags;

public interface RaceLogFlagEvent extends RaceLogEvent {

    Flags getUpperFlag();

    Flags getLowerFlag();

    boolean isDisplayed();
}
