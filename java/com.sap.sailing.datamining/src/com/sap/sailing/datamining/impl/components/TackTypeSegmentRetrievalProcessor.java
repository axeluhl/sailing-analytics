package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTackTypeSegmentContext;
import com.sap.sailing.datamining.impl.data.GPSFixTrackWithContext;
import com.sap.sailing.datamining.impl.data.TackTypeSegmentWithContext;
import com.sap.sailing.datamining.shared.TackTypeSegmentsDataMiningSettings;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TackType;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class TackTypeSegmentRetrievalProcessor extends AbstractRetrievalProcessor<HasRaceOfCompetitorContext, HasTackTypeSegmentContext> {
    private final TackTypeSegmentsDataMiningSettings settings;

    public TackTypeSegmentRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasTackTypeSegmentContext, ?>> resultReceivers,
            TackTypeSegmentsDataMiningSettings settings, int retrievalLevel, String retrievedDataTypeMessageKey) {
        super(HasRaceOfCompetitorContext.class, HasTackTypeSegmentContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
        this.settings = settings;
    }

    @Override
    public Iterable<HasTackTypeSegmentContext> retrieveData(HasRaceOfCompetitorContext element) {
        List<HasTackTypeSegmentContext> tackTypeSegments = new ArrayList<>();
        final TrackedRace trackedRace = element.getTrackedRaceContext().getTrackedRace();
        final TimePoint startOfRace = element.getTrackedRaceContext().getTrackedRace().getStartOfRace();
        final Competitor competitor = element.getCompetitor();
        if (startOfRace != null) {
            final GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack = trackedRace.getTrack(element.getCompetitor());
            if (gpsFixTrack != null) {
                final Iterator<MarkPassing> markPassingIterator = trackedRace.getMarkPassings(competitor).iterator();
                if (markPassingIterator.hasNext()) { // only search for tack type segments if the competitor has started the race
                    MarkPassing legStartMarkPassing = markPassingIterator.next();
                    TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLegStartingAt(legStartMarkPassing.getWaypoint()).getTrackedLeg(competitor);
                    MarkPassing nextMarkPassing = markPassingIterator.hasNext() ? markPassingIterator.next() : null;
                    final Waypoint finish = trackedRace.getRace().getCourse().getLastWaypoint();
                    TackType currentTackType = null;
                    boolean segmentEmpty = true;
                    TackType nextTackType = null;
                    TimePoint startOfCurrentSegment = legStartMarkPassing.getTimePoint();
                    GPSFix gpsFix = null;
                    gpsFixTrack.lockForRead();
                    try {
                        // run through all of the competitor's fixes, starting at the first mark passing and finishing when there are
                        // no more fixes left or the finish mark passing has been reached
                        for (final Iterator<GPSFixMoving> i=gpsFixTrack.getFixesIterator(startOfCurrentSegment, /* fromInclusive */ true);
                             i.hasNext() && legStartMarkPassing.getWaypoint() != finish; ) {
                            if (isAborted()) {
                                break;
                            }
                            gpsFix = i.next();
                            while (nextMarkPassing != null && !gpsFix.getTimePoint().before(nextMarkPassing.getTimePoint())) {
                                // reached leg's end; complete the current segment if not empty and move through mark passings
                                // iterator until having found the leg the gpsFix is in, while moving the trackedLegOfCompetitor along
                                legStartMarkPassing = nextMarkPassing;
                                if (!segmentEmpty) {
                                    addOrMergeTackTypeSegment(element, tackTypeSegments, gpsFixTrack, startOfCurrentSegment,
                                            nextMarkPassing.getTimePoint(), currentTackType);
                                    segmentEmpty = gpsFix.getTimePoint().equals(nextMarkPassing.getTimePoint()); // if the fix is exactly at leg start
                                    startOfCurrentSegment = nextMarkPassing.getTimePoint();
                                }
                                // if nextMarkPassing is last MarkPassing there will be no leg after, trackedLeg is null
                                trackedLegOfCompetitor = nextMarkPassing.getWaypoint()==finish ? null : trackedRace.getTrackedLegStartingAt(nextMarkPassing.getWaypoint()).getTrackedLeg(competitor);
                                nextMarkPassing = markPassingIterator.hasNext() ? markPassingIterator.next() : null;
                            }
                            try {
                                // no null case like line 87, because for loop checks that mark passing at leg start is not finish mark passing
                                nextTackType = trackedLegOfCompetitor == null ? null : trackedLegOfCompetitor.getTackType(gpsFix.getTimePoint());
                            } catch (NoWindException e) {
                                nextTackType = null;
                            }
                            // invariant: nextMarkPassing is either null or after gpsFix's time and representing the passing of the mark at the end
                            // of the leg gpsFix is in; trackedLegOfCompetitor corresponds to the leg gpsFix is in
                            if (!segmentEmpty && nextTackType != currentTackType) {
                                addOrMergeTackTypeSegment(element, tackTypeSegments, gpsFixTrack, startOfCurrentSegment,
                                        gpsFix.getTimePoint() /* don't include the last interval ending at the non-TackType fix */, currentTackType);
                                segmentEmpty = true; // because gpsFix is now exactly at the beginning of the new segment
                                startOfCurrentSegment = gpsFix.getTimePoint();
                            } else {
                                segmentEmpty = false; // gpsFix is in the current segment
                            }
                            currentTackType = nextTackType;
                        }
                    } finally {
                        gpsFixTrack.unlockAfterRead();
                    }
                    if (!segmentEmpty) {
                        addOrMergeTackTypeSegment(element, tackTypeSegments, gpsFixTrack, startOfCurrentSegment, gpsFix.getTimePoint(), currentTackType);
                        // no need to update segmentEmpty / startOfCurrentSegment / lastTackType because now we're done
                    }
                }
            }
        }
        return tackTypeSegments;
    }

    private void addOrMergeTackTypeSegment(HasRaceOfCompetitorContext element,
            List<HasTackTypeSegmentContext> tackTypeSegments, final GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack,
            TimePoint startOfSegment, TimePoint endOfSegment, TackType tackType) {
        if (settings.getMinimumTackTypeSegmentDuration() == null || startOfSegment.until(endOfSegment)
                .compareTo(settings.getMinimumTackTypeSegmentDuration()) >= 0) {
            if (tackTypeSegments.isEmpty() || settings.getMinimumDurationBetweenAdjacentTackTypeSegments() == null) {
                tackTypeSegments.add(createTackTypeSegment(startOfSegment, endOfSegment, element, gpsFixTrack, tackType));
            } else {
                // we wouldn't want to merge segments with different tack types; different from foiling segments where you *would* want to join to closely-adjacent foiled segments
                final HasTackTypeSegmentContext previousSegment = tackTypeSegments.get(tackTypeSegments.size()-1);
                final TimePoint previousEnd = previousSegment.getEndOfTackTypeSegment();
                if (previousSegment.getTackType() == tackType && previousEnd.until(startOfSegment).compareTo(settings.getMinimumDurationBetweenAdjacentTackTypeSegments()) < 0) {
                    // merge:
                    tackTypeSegments.set(tackTypeSegments.size()-1, createTackTypeSegment(previousSegment.getStartOfTackTypeSegment(), endOfSegment, element, gpsFixTrack, tackType));
                } else {
                    // add; duration between the segments is large enough or tack type is different
                    tackTypeSegments.add(createTackTypeSegment(startOfSegment, endOfSegment, element, gpsFixTrack, tackType));
                }
            }
        }
    }

    private HasTackTypeSegmentContext createTackTypeSegment(TimePoint startOfSegment, TimePoint endOfSegment,
            HasRaceOfCompetitorContext raceOfCompetitorContext, GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack, TackType tackType) {
        return new TackTypeSegmentWithContext(new GPSFixTrackWithContext(raceOfCompetitorContext, gpsFixTrack), startOfSegment, endOfSegment, tackType); 
    }

}