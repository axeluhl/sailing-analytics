package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;

/**
 * When TMD messages about mark passings arrive with their race start time-relative time stamp, and no race start time
 * has been received yet, the TMD message contents need to be queued and re-applied once a start time for the race was
 * received. The queuing is managed by this class.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class TMDMessageQueue {
    private static final Logger logger = Logger.getLogger(TMDMessageQueue.class.getName());
    
    private static class TMDMessageContents {
        private final String raceID;
        private final String competitorIdAsString;
        private final List<Util.Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds;

        public TMDMessageContents(String raceID, String competitorIdAsString,
                List<Util.Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds) {
            super();
            this.raceID = raceID;
            this.competitorIdAsString = competitorIdAsString;
            this.markIndicesRanksAndTimesSinceStartInMilliseconds = markIndicesRanksAndTimesSinceStartInMilliseconds;
        }

        public String getRaceID() {
            return raceID;
        }

        public String getCompetitorIdAsString() {
            return competitorIdAsString;
        }

        public List<Util.Triple<Integer, Integer, Long>> getMarkIndicesRanksAndTimesSinceStartInMilliseconds() {
            return markIndicesRanksAndTimesSinceStartInMilliseconds;
        }
        
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("TMD|"+getRaceID()+"|"+getCompetitorIdAsString()+"|");
            result.append(getMarkIndicesRanksAndTimesSinceStartInMilliseconds().size());
            result.append('|');
            for (Util.Triple<Integer, Integer, Long> markIndexRankAndTimeSinceStartInMilliseconds : getMarkIndicesRanksAndTimesSinceStartInMilliseconds()) {
                result.append(markIndexRankAndTimeSinceStartInMilliseconds.getA());
                result.append(';');
                if (markIndexRankAndTimeSinceStartInMilliseconds.getB() != null) {
                    result.append(markIndexRankAndTimeSinceStartInMilliseconds.getB());
                }
                result.append(";<");
                result.append(markIndexRankAndTimeSinceStartInMilliseconds.getC());
                result.append("ms>");
            }
            return result.toString();
        }
    }

    private final SwissTimingRaceTrackerImpl raceTracker;
    
    private final Set<TMDMessageContents> queuedMessages;
    
    public TMDMessageQueue(SwissTimingRaceTrackerImpl raceTracker) {
        super();
        this.raceTracker = raceTracker;
        queuedMessages = new HashSet<>();
    }
    
    public synchronized void enqueue(String raceID, String competitorIdAsString, List<Util.Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds) {
        final TMDMessageContents message = new TMDMessageContents(raceID, competitorIdAsString, markIndicesRanksAndTimesSinceStartInMilliseconds);
        queuedMessages.add(message);
        logger.info("Queued TMD message "+message+" for replay when start time has been received");
    }
    
    /**
     * The tracker shall call this when a start time has been received. In case there are any messages in this queue, they will
     * be re-sent to the tracker where they now can be evaluated properly. Precondition to calling this method is that the start
     * time was set on the {@link TrackedRace} managed by the {@link SwissTimingRaceTrackerImpl}.
     */
    public synchronized void validStartTimeReceived() {
        DynamicTrackedRace trackedRace = raceTracker.getTrackedRace();
        Iterator<TMDMessageContents> i = queuedMessages.iterator();
        Map<Competitor, TreeMap<Integer, MarkPassing>> cleansedMarkPassings = new HashMap<>();
        // first remove all mark passings that will then be updated to make sure no "guessed time" remains;
        // otherwise, this could lead to the "guessed time" to 
        while (i.hasNext()) {
            TMDMessageContents messageContents = i.next();
            Competitor competitor = raceTracker.getDomainFactory().getBaseDomainFactory().getExistingCompetitorById(UUID.fromString(messageContents.getCompetitorIdAsString()));
            if (competitor == null) {
                logger.info("Received timing data for boat ID " + messageContents.getCompetitorIdAsString() + " in race " + messageContents.getRaceID()
                        + " but couldn't find a competitor with that boat ID in this race. Ignoring.");
            } else {
                NavigableSet<MarkPassing> oldMarkPassings = trackedRace.getMarkPassings(competitor);
                TreeMap<Integer, MarkPassing> cleansedMarkPassingsForCompetitor = cleansedMarkPassings.get(competitor);
                if (cleansedMarkPassingsForCompetitor == null) {
                    cleansedMarkPassingsForCompetitor = new TreeMap<>();
                    cleansedMarkPassings.put(competitor, cleansedMarkPassingsForCompetitor);
                    Course course = trackedRace.getRace().getCourse();
                    course.lockForRead();
                    try {
                        trackedRace.lockForRead(oldMarkPassings);
                        try {
                            for (MarkPassing oldMarkPassing : oldMarkPassings) {
                                int waypointIndex = course.getIndexOfWaypoint(oldMarkPassing.getWaypoint());
                                cleansedMarkPassingsForCompetitor.put(waypointIndex, oldMarkPassing);
                            }
                        } finally {
                            trackedRace.unlockAfterRead(oldMarkPassings);
                        }
                    } finally {
                        course.unlockAfterRead();
                    }
                }
                // remove those mark passings for which the TMD message has mark passing times; their time points would
                // just have been guessed.
                // This will avoid that the start time inference
                // rules consider them and let them take precedence over the start time received
                for (Util.Triple<Integer, Integer, Long> markIndexRankAndTimeSinceStartInMilliseconds : messageContents
                        .getMarkIndicesRanksAndTimesSinceStartInMilliseconds()) {
                    if (cleansedMarkPassingsForCompetitor.containsKey(markIndexRankAndTimeSinceStartInMilliseconds
                            .getA())) {
                        logger.info("Removing mark passing for mark #"
                                + markIndexRankAndTimeSinceStartInMilliseconds.getA()
                                + " for competitor "
                                + competitor.getName()
                                + " because its time point "
                                + cleansedMarkPassingsForCompetitor.get(
                                        markIndexRankAndTimeSinceStartInMilliseconds.getA()).getTimePoint()
                                + " was guessed; will replace momentarily...");
                        cleansedMarkPassingsForCompetitor.remove(markIndexRankAndTimeSinceStartInMilliseconds.getA());
                    }
                }
            }
        }
        for (Map.Entry<Competitor, TreeMap<Integer, MarkPassing>> e : cleansedMarkPassings.entrySet()) {
            trackedRace.updateMarkPassings(e.getKey(), e.getValue().values());
        }
        i = queuedMessages.iterator();
        while (i.hasNext()) {
            TMDMessageContents messageContents = i.next();
            logger.info("Re-Playing TMD message "+messageContents+" with new race start time "+trackedRace.getStartOfRace());
            raceTracker.receivedTimingData(messageContents.getRaceID(), messageContents.getCompetitorIdAsString(),
                    messageContents.getMarkIndicesRanksAndTimesSinceStartInMilliseconds());
            i.remove();
        }
    }
}
