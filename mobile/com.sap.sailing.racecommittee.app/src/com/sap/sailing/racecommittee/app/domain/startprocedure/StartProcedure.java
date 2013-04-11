package com.sap.sailing.racecommittee.app.domain.startprocedure;

import java.util.List;

import android.content.Context;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.EssStartPhaseEventListener;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public interface StartProcedure {
    
    TimePoint getStartPhaseStartTime(TimePoint startTime);
    
    /**
     * Calculates the logical event time for a given start time. When {@link MillisecondsTimePoint}.now() is before the begin of the start phase, now() is returned as event time.
     * When now() is after the logical timepoint at which the start phase begins, a timepoint one millisecond before the begin of the start phase is returned as start time
     * @param newEnteredStartTime the new start time entered by the race committee
     * @return the logical correct event time of the new start time race log event
     */
    TimePoint getLogicalStartTimeEventTime(TimePoint newEnteredStartTime);
    
    List<TimePoint> getAutomaticEventFireTimePoints(TimePoint startTime);
    
    void dispatchFiredEventTimePoint(TimePoint startTime, TimePoint eventTime);
    
    void setRaceStateChangedListener(StartProcedureRaceStateChangedListener raceStateChangedListener);
    
    void setFinishing(TimePoint eventTime);
    
    void dispatchAutomaticRaceEndEvent(TimePoint eventTime);
    
    void setFinished(TimePoint eventTime);
    
    void setPostponed(TimePoint eventTime, Flags lowerFlag);
    
    void setAbandoned(TimePoint eventTime, Flags lowerFlag);
    
    void setGeneralRecall(TimePoint eventTime);
    
    void setIndividualRecall(TimePoint eventTime);

    void dispatchFiredIndividualRecallRemovalEvent(TimePoint individualRecallDisplayedTime, TimePoint eventTime);
    
    void setIndividualRecallRemoval(TimePoint eventTime);
    
    Class<? extends RaceFragment> getStartphaseFragment();
    
    void setEssStartPhaseEventListener(EssStartPhaseEventListener listener);
    
    Pair<String, Long> getNextFlagCountdownUiLabel(Context context, long millisecondsTillStart);
}
