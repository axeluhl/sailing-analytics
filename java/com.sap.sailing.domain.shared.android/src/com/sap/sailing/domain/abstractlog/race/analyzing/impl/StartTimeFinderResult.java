package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class StartTimeFinderResult {

    private List<SimpleRaceLogIdentifier> racesDependingOn;
    private TimePoint startTime;
    
    public StartTimeFinderResult() {
        racesDependingOn = new ArrayList<SimpleRaceLogIdentifier>();
    }
    
    public StartTimeFinderResult(TimePoint startTime) {
        racesDependingOn = new ArrayList<SimpleRaceLogIdentifier>();
        this.startTime = startTime;
    }
    
    public StartTimeFinderResult(List<SimpleRaceLogIdentifier> racesDependingOn, TimePoint startTime) {
        this.racesDependingOn = racesDependingOn;
        this.startTime = startTime;
    }

    public List<SimpleRaceLogIdentifier> getRacesDependingOn() {
        return racesDependingOn;
    }

    public TimePoint getStartTime() {
        return startTime;
    }
    
    void setStartTime(TimePoint startTime){
        this.startTime = startTime;
    }
    
    void addToStartTime(Duration toAdd){
        if (startTime == null){
            return;
        }
        
        startTime = startTime.plus(toAdd);
    }
    
    void addToDependencyList(SimpleRaceLogIdentifier race){
        racesDependingOn.add(race);
    }
    
    void addToDependencyList(List<SimpleRaceLogIdentifier> race){
        racesDependingOn.addAll(race);
    }

    void combineWith(StartTimeFinderResult other){
        this.addToDependencyList(other.getRacesDependingOn());
        this.setStartTime(other.getStartTime());
    }
    
    public boolean isDependentStartTime(){
        if (racesDependingOn == null){
            return false;
        }
        
        return racesDependingOn.isEmpty();
    }
}
