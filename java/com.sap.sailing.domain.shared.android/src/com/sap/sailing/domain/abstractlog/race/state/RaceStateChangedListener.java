package com.sap.sailing.domain.abstractlog.race.state;

public interface RaceStateChangedListener {
    
    void onRacingProcedureChanged(ReadonlyRaceState state);
    void onStatusChanged(ReadonlyRaceState state);
    void onStartTimeChanged(ReadonlyRaceState state);
    void onFinishingTimeChanged(ReadonlyRaceState state);
    void onFinishedTimeChanged(ReadonlyRaceState state);
    void onProtestTimeChanged(ReadonlyRaceState state);
    void onAdvancePass(ReadonlyRaceState state);
    /*
     * TODO: We need to recheck the AbortFinder to enable these interface methods
     * void onAborted(RaceState2 state);
     * void onGeneralRecall(RaceState2 state);*/
    void onFinishingPositioningsChanged(ReadonlyRaceState state);
    void onFinishingPositionsConfirmed(ReadonlyRaceState state);
    void onCourseDesignChanged(ReadonlyRaceState state);
    void onWindFixChanged(ReadonlyRaceState state);

}
