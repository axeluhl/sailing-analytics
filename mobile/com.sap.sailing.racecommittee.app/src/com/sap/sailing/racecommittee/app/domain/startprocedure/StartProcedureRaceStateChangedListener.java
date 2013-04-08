package com.sap.sailing.racecommittee.app.domain.startprocedure;

import com.sap.sailing.domain.common.TimePoint;

public interface StartProcedureRaceStateChangedListener {
    void onRaceAborted(TimePoint eventTime);
    
    void onRaceStartphaseEntered(TimePoint eventTime);

    void onRaceStarted(TimePoint eventTime);

    void onRaceFinishing(TimePoint now);
    
    void onRaceFinished(TimePoint now);
    
    void onIndividualRecall(TimePoint eventTime);

    void onIndividualRecallRemoval();
}
