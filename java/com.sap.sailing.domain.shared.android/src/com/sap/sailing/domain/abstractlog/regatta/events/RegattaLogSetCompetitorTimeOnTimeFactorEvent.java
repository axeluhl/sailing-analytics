package com.sap.sailing.domain.abstractlog.regatta.events;

public interface RegattaLogSetCompetitorTimeOnTimeFactorEvent extends RegattaLogSetCompetitorHandicapInfoEvent {
    Double getTimeOnTimeFactor();
}
