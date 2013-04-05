package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * When TMD messages about mark passings arrive with their race start time-relative time stamp, and no race start time
 * has been received yet, the TMD message contents need to be queued and re-applied once a start time for the race was
 * received. The queuing is managed by this class.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class TMDMessageQueue {
    private static class TMDMessageContents {
        private final String raceID;
        private final String boatID;
        private final List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds;

        public TMDMessageContents(String raceID, String boatID,
                List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds) {
            super();
            this.raceID = raceID;
            this.boatID = boatID;
            this.markIndicesRanksAndTimesSinceStartInMilliseconds = markIndicesRanksAndTimesSinceStartInMilliseconds;
        }

        public String getRaceID() {
            return raceID;
        }

        public String getBoatID() {
            return boatID;
        }

        public List<Triple<Integer, Integer, Long>> getMarkIndicesRanksAndTimesSinceStartInMilliseconds() {
            return markIndicesRanksAndTimesSinceStartInMilliseconds;
        }
    }

    private final SwissTimingRaceTrackerImpl raceTracker;
    
    private final Set<TMDMessageContents> queuedMessages;
    
    public TMDMessageQueue(SwissTimingRaceTrackerImpl raceTracker) {
        super();
        this.raceTracker = raceTracker;
        queuedMessages = new HashSet<>();
    }
    
    public synchronized void enqueue(String raceID, String boatID, List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds) {
        queuedMessages.add(new TMDMessageContents(raceID, boatID, markIndicesRanksAndTimesSinceStartInMilliseconds));
    }
    
    /**
     * The tracker shall call this when a start time has been received. In case there are any messages in this queue, they will
     * be re-sent to the tracker where they now can be evaluated properly. Precondition to calling this method is that the start
     * time was set on the {@link TrackedRace} managed by the {@link SwissTimingRaceTrackerImpl}.
     */
    public synchronized void validStartTimeReceived() {
        Iterator<TMDMessageContents> i = queuedMessages.iterator();
        while (i.hasNext()) {
            TMDMessageContents messageContents = i.next();
            DynamicTrackedRace trackedRace = raceTracker.getTrackedRace();
            Competitor competitor = raceTracker.getDomainFactory().getCompetitorByBoatIDAndBoatClass(messageContents.getBoatID(),
                    trackedRace.getRace().getBoatClass());
            NavigableSet<MarkPassing> oldMarkPassings = trackedRace.getMarkPassings(competitor);
            Map<Integer, MarkPassing> cleansedMarkPassings = new HashMap<>();
            Course course = trackedRace.getRace().getCourse();
            course.lockForRead();
            try {
                trackedRace.lockForRead(oldMarkPassings);
                try {
                    for (MarkPassing oldMarkPassing : oldMarkPassings) {
                        int waypointIndex = course.getIndexOfWaypoint(oldMarkPassing.getWaypoint());
                        cleansedMarkPassings.put(waypointIndex, oldMarkPassing);
                    }
                } finally {
                    trackedRace.unlockAfterRead(oldMarkPassings);
                }
            } finally {
                course.unlockAfterRead();
            }
            // remove those mark passings for which the TMD message has mark passing times; their time points were just guessed.
            // This will avoid that the start time inference
            // rules consider them and let them take precedence over the start time received
            for (Triple<Integer, Integer, Long> markIndexRankAndTimeSinceStartInMilliseconds : messageContents.getMarkIndicesRanksAndTimesSinceStartInMilliseconds()) {
                cleansedMarkPassings.remove(markIndexRankAndTimeSinceStartInMilliseconds.getA());
            }
            trackedRace.updateMarkPassings(competitor, cleansedMarkPassings.values());
            raceTracker.receivedTimingData(messageContents.getRaceID(), messageContents.getBoatID(),
                    messageContents.getMarkIndicesRanksAndTimesSinceStartInMilliseconds());
            i.remove();
        }
    }
}
