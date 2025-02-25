package com.sap.sailing.aiagent.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.aiagent.impl.rules.GoodStartRule;
import com.sap.sailing.aiagent.impl.rules.Rule;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.AddResult;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sse.common.TimePoint;

/**
 * Used to observe one or more races, usually of the same event, for changes that may be relevant for one or more
 * {@link Rule}s. Any change event received in this object's role of a {@link RaceChangeListener} is forwarded to the
 * {@link #rules} it knows.
 * <p>
 * 
 * The constructor defines the set of rules to evaluate; those will then
 * {@link AIAgentImpl#produceCommentFromPrompt(String, String, String, String, TimePoint) produce comments} that are
 * inserted as tags / comments for the race.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RaceListener implements RaceChangeListener {
    private final Iterable<Rule> rules;
    
    public RaceListener(AIAgentImpl aiAgent, Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        super();
        final List<Rule> myRules = new ArrayList<>();
        myRules.add(new GoodStartRule(aiAgent, leaderboard, raceColumn, fleet, trackedRace));
        this.rules = myRules;
    }

    @Override
    public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
        rules.forEach(r->r.waypointAdded(zeroBasedIndex, waypointThatGotAdded));
    }

    @Override
    public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        rules.forEach(r->r.waypointRemoved(zeroBasedIndex, waypointThatGotRemoved));
        
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor, AddResult addedOrReplaced) {
        rules.forEach(r->r.competitorPositionChanged(fix, competitor, addedOrReplaced));
    }

    @Override
    public void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack, AddResult addedOrReplaced) {
        rules.forEach(r->r.markPositionChanged(fix, mark, firstInTrack, addedOrReplaced));
    }

    @Override
    public void firstGPSFixReceived() {
        rules.forEach(r->r.firstGPSFixReceived());
    }

    @Override
    public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
            Iterable<MarkPassing> markPassings) {
        rules.forEach(r->r.markPassingReceived(competitor, oldMarkPassings, markPassings));
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        rules.forEach(r->r.speedAveragingChanged(oldMillisecondsOverWhichToAverage, newMillisecondsOverWhichToAverage));
    }

    @Override
    public void windDataReceived(Wind wind, WindSource windSource) {
        rules.forEach(r->r.windDataReceived(wind, windSource));
    }

    @Override
    public void windDataRemoved(Wind wind, WindSource windSource) {
        rules.forEach(r->r.windDataRemoved(wind, windSource));
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        rules.forEach(r->r.windAveragingChanged(oldMillisecondsOverWhichToAverage, newMillisecondsOverWhichToAverage));
    }

    @Override
    public void startOfTrackingChanged(TimePoint oldStartOfTracking, TimePoint newStartOfTracking) {
        rules.forEach(r->r.startOfTrackingChanged(oldStartOfTracking, newStartOfTracking));
    }

    @Override
    public void endOfTrackingChanged(TimePoint oldEndOfTracking, TimePoint newEndOfTracking) {
        rules.forEach(r->r.endOfTrackingChanged(oldEndOfTracking, newEndOfTracking));
    }

    @Override
    public void startTimeReceivedChanged(TimePoint startTimeReceived) {
        rules.forEach(r->r.startTimeReceivedChanged(startTimeReceived));
    }

    @Override
    public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
        rules.forEach(r->r.startOfRaceChanged(oldStartOfRace, newStartOfRace));
    }

    @Override
    public void finishingTimeChanged(TimePoint oldFinishingTime, TimePoint newFinishingTime) {
        rules.forEach(r->r.finishingTimeChanged(oldFinishingTime, newFinishingTime));
    }

    @Override
    public void finishedTimeChanged(TimePoint oldFinishedTime, TimePoint newFinishedTime) {
        rules.forEach(r->r.finishedTimeChanged(oldFinishedTime, newFinishedTime));
    }

    @Override
    public void delayToLiveChanged(long delayToLiveInMillis) {
        rules.forEach(r->r.delayToLiveChanged(delayToLiveInMillis));
    }

    @Override
    public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
        rules.forEach(r->r.windSourcesToExcludeChanged(windSourcesToExclude));
    }

    @Override
    public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
        rules.forEach(r->r.statusChanged(newStatus, oldStatus));
    }

    @Override
    public void competitorSensorTrackAdded(DynamicSensorFixTrack<Competitor, ?> track) {
        rules.forEach(r->r.competitorSensorTrackAdded(track));
    }

    @Override
    public void competitorSensorFixAdded(Competitor competitor, String trackName, SensorFix fix,
            AddResult addedOrReplaced) {
        rules.forEach(r->r.competitorSensorFixAdded(competitor, trackName, fix, addedOrReplaced));
    }

    @Override
    public void regattaLogAttached(RegattaLog regattaLog) {
        rules.forEach(r->r.regattaLogAttached(regattaLog));
    }

    @Override
    public void raceLogAttached(RaceLog raceLog) {
        rules.forEach(r->r.raceLogAttached(raceLog));
    }

    @Override
    public void raceLogDetached(RaceLog raceLog) {
        rules.forEach(r->r.raceLogDetached(raceLog));
    }
}
