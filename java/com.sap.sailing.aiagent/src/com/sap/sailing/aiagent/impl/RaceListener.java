package com.sap.sailing.aiagent.impl;

import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.tracking.AddResult;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sse.common.TimePoint;

public class RaceListener implements RaceChangeListener {
    @Override
    public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor, AddResult addedOrReplaced) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack, AddResult addedOrReplaced) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void firstGPSFixReceived() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
            Iterable<MarkPassing> markPassings) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windDataReceived(Wind wind, WindSource windSource) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windDataRemoved(Wind wind, WindSource windSource) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void startOfTrackingChanged(TimePoint oldStartOfTracking, TimePoint newStartOfTracking) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void endOfTrackingChanged(TimePoint oldEndOfTracking, TimePoint newEndOfTracking) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void startTimeReceivedChanged(TimePoint startTimeReceived) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finishingTimeChanged(TimePoint oldFinishingTime, TimePoint newFinishingTime) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finishedTimeChanged(TimePoint oldFinishedTime, TimePoint newFinishedTime) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void delayToLiveChanged(long delayToLiveInMillis) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void competitorSensorTrackAdded(DynamicSensorFixTrack<Competitor, ?> track) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void competitorSensorFixAdded(Competitor competitor, String trackName, SensorFix fix,
            AddResult addedOrReplaced) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void regattaLogAttached(RegattaLog regattaLog) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void raceLogAttached(RaceLog raceLog) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void raceLogDetached(RaceLog raceLog) {
        // TODO Auto-generated method stub
        
    }

}
