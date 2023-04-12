package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.common.racelog.Flags;

/**
 * Finds the last valid "abort" flag ({@link Flags#AP}, {@link Flags#NOVEMBER}, or {@link Flags#FIRSTSUBSTITUTE}) that
 * has been displayed in the last-but-one pass, regardless the state of the current pass. This assumes that
 * aborting a pass with one of these flags will automatically trigger a {@link RaceLogPassChangeEvent}, advancing
 * immediately to the next pass.<p>
 * 
 * This finder does <em>not<em> inspect the <em>current</em> pass, so simply finding a valid abort flag in the
 * last-but-one pass with this finder does not necessarily mean that it would still have to be displayed. If,
 * for example, the current pass has already set a start time again then this supersedes the race abort, postponing
 * or general recall of the previous pass.
 */
public class AbortingFlagFinder extends RaceLogAnalyzer<RaceLogFlagEvent> {

    public AbortingFlagFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RaceLogFlagEvent performAnalysis() {
        if (getLog().getCurrentPassId() <= RaceLog.DefaultPassId) {
            return null;
        }
        int lastButOnePass = getLog().getCurrentPassId() - 1;
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event.getPassId() == lastButOnePass && event instanceof RaceLogFlagEvent) {
                RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) event;
                if (isDisplayedAbortingFlag(flagEvent)) {
                    return flagEvent;
                }
            }
        }
        return null;
    }

    private static boolean isDisplayedAbortingFlag(RaceLogFlagEvent flagEvent) {
        return (flagEvent.getUpperFlag().equals(Flags.AP) && flagEvent.isDisplayed())
                || (flagEvent.getUpperFlag().equals(Flags.NOVEMBER) && flagEvent.isDisplayed())
                || (flagEvent.getUpperFlag().equals(Flags.FIRSTSUBSTITUTE) && flagEvent.isDisplayed());
    }

}
