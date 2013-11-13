package com.sap.sailing.domain.racelog.state;


public interface RaceState2ChangedListener {
    
    void onRacingProcedureChanged(RaceState2 state);
    void onStatusChanged(RaceState2 state);
    void onStartTimeChanged(RaceState2 state);
    void onFinishingTimeChanged(RaceState2 state);
    void onFinishedTimeChanged(RaceState2 state);
    void onProtestTimeChanged(RaceState2 state);
    void onAdvancePass(RaceState2 state);
    /*void onAborted(RaceState2 state);
    void onGeneralRecall(RaceState2 state);*/
    void onFinishingPositioningsChanged(RaceState2 state);
    void onFinishingPositionsConfirmed(RaceState2 state);
    void onCourseDesignChanged(RaceState2 state);

}
