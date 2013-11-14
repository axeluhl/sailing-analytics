package com.sap.sailing.domain.racelog.state;


public interface RaceStateChangedListener {
    
    void onRacingProcedureChanged(RaceState state);
    void onStatusChanged(RaceState state);
    void onStartTimeChanged(RaceState state);
    void onFinishingTimeChanged(RaceState state);
    void onFinishedTimeChanged(RaceState state);
    void onProtestTimeChanged(RaceState state);
    void onAdvancePass(RaceState state);
    /*void onAborted(RaceState2 state);
    void onGeneralRecall(RaceState2 state);*/
    void onFinishingPositioningsChanged(RaceState state);
    void onFinishingPositionsConfirmed(RaceState state);
    void onCourseDesignChanged(RaceState state);

}
