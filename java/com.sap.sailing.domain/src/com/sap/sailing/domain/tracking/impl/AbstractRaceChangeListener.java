package com.sap.sailing.domain.tracking.impl;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.TimePoint;

/**
 * Delegates all listener operations to a default action which is implemented here to do nothing. This way, subclasses
 * can easily provide a default action to be executed for all callback methods except for maybe a few which then need to
 * be explicitly overridden. If not most methods need to perform the same action then instead of implementing a default
 * action subclasses should rather override the {@link RaceChangeListener} operations individually.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractRaceChangeListener implements RaceChangeListener {
    /**
     * This implementation of the default action to which all other operations delegate does nothing. Subclasses can
     * choose to override this method to provide this as the default behavior for all other methods and then still
     * override individual {@link RaceChangeListener} methods, or they can leave the default action empty and just
     * override {@link RaceChangeListener} methods.
     */
    protected void defaultAction() {
        // Does nothing
    }

    @Override
    public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
        defaultAction();
    }

    @Override
    public void statusChanged(TrackedRaceStatus newStatus) {
        defaultAction();
    }

    @Override
    public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
        defaultAction();
    }

    @Override
    public void startOfTrackingChanged(TimePoint startOfTracking) {
        defaultAction();
    }

    @Override
    public void endOfTrackingChanged(TimePoint endOfTracking) {
        defaultAction();
    }

    @Override
    public void startTimeReceivedChanged(TimePoint startTimeReceived) {
        defaultAction();
    }

    @Override
    public void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack) {
        defaultAction();
    }

    @Override
    public void windDataReceived(Wind wind, WindSource windSource) {
        defaultAction();
    }

    @Override
    public void windDataRemoved(Wind wind, WindSource windSource) {
        defaultAction();
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        defaultAction();
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
        defaultAction();
    }

    @Override
    public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings, Iterable<MarkPassing> markPassings) {
        defaultAction();
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        defaultAction();
    }

    @Override
    public void delayToLiveChanged(long delayToLiveInMillis) {
        defaultAction();
    }

    @Override
    public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
        defaultAction();
    }

    @Override
    public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        defaultAction();
    }
    
}
