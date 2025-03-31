package com.sap.sailing.domain.abstractlog.regatta.events;

public interface RegattaLogSetCompetitorTimeOnTimeFactorEvent extends RegattaLogSetCompetitorHandicapInfoEvent {
    /**
     * @return a {@link Double#isFinite(double) finite} value
     */
    Double getTimeOnTimeFactor();
}
