package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasTackTypeSegmentContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.TackTypeSegmentWithContext;
import com.sap.sailing.datamining.shared.TackTypeSegmentsDataMiningSettings;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TackType;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.datamining.impl.data.GPSFixTrackWithContext;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
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
    protected Iterable<HasTackTypeSegmentContext> retrieveData(HasRaceOfCompetitorContext element) {
        List<HasTackTypeSegmentContext> tackTypeSegments = new ArrayList<>();
        final TrackedRace trackedRace = element.getTrackedRaceContext().getTrackedRace();
        final TimePoint startOfRace = element.getTrackedRaceContext().getTrackedRace().getStartOfRace();
        if (startOfRace != null) {
            final TimePoint endOfRace = element.getTrackedRaceContext().getTrackedRace().getEndOfRace();
            final TimePoint end;
            if (endOfRace == null) {
                final TimePoint endOfTracking = element.getTrackedRaceContext().getTrackedRace().getEndOfTracking();
                end = endOfTracking == null ? MillisecondsTimePoint.now() : endOfTracking;
            } else {
                end = endOfRace;
            }
            final GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack = trackedRace.getTrack(element.getCompetitor());
            final Iterator<Waypoint> wayPointPassingIterator = element.getTrackedRaceContext().getTrackedRace().getRace().getCourse().getWaypoints().iterator();
            if (gpsFixTrack != null) {
                TackType lastTackType = null;
                TackType currentFixTackType = null;
                TimePoint last = null;
                TimePoint startOfSegment = null;
                gpsFixTrack.lockForRead();
                try {
                    TrackedRace trackedRaceComp = element.getTrackedRaceContext().getTrackedRace();
                        while (wayPointPassingIterator.hasNext()) {
                            TimePoint time = trackedRaceComp.getMarkPassing(element.getCompetitor(), wayPointPassingIterator.next()).getTimePoint();
                            TimePoint time2 = null;
                            try {
                                time2 = trackedRaceComp
                                        .getMarkPassing(element.getCompetitor(), wayPointPassingIterator.next())
                                        .getTimePoint();
                            } catch (NullPointerException e) {
                                time2 = endOfRace;
                            }
                            TrackedLegOfCompetitor trackedLegComp = trackedRaceComp
                                    .getTrackedLeg(element.getCompetitor(), time);

                        for (final GPSFix gpsFix : gpsFixTrack.getFixes(time, /* fromInclusive */ true, time2,
                                /* toInclusive */ false)) { // SCHLEIFE die durch alle gps fixes des rennen geht
//                            if (trackedLegComp == null || gpsFix.getTimePoint().after(trackedLegComp.getFinishTime())) {
//                                trackedLegComp = trackedRaceComp.getTrackedLeg(element.getCompetitor(),
//                                        gpsFix.getTimePoint());
//                            }
                            if (isAborted()) {
                                break;
                            }
                            try {
                                currentFixTackType = trackedLegComp.getTackType(gpsFix.getTimePoint()); // tacktype mit current fix, sobald fix in erstem leg ist
                            } catch (NoWindException e) {
                                currentFixTackType = null;
                            }
                            if (currentFixTackType != lastTackType) { // wenn Veränderung des TT:
                                if (settings.getMinimumTackTypeSegmentDuration() == null || startOfSegment.until(last) // wenn nicht zu kleine segment dann:
                                        .compareTo(settings.getMinimumTackTypeSegmentDuration()) >= 0) {
                                    addOrMergeTackTypeSegment(element, tackTypeSegments, gpsFixTrack, startOfSegment,
                                            last /*don't include the last interval ending at the non-TackType fix  */);
                                } // wenn next abschnitt nicht null TT ist:
                                if (currentFixTackType != null) { // immer wenn TT sich ändert wird current tacktype und lasttacktype geupdated, außer bei null
                                    startOfSegment = gpsFix.getTimePoint();
                                    lastTackType = currentFixTackType;
                                } else {
                                    startOfSegment = null;
                                }
                            }
                            last = gpsFix.getTimePoint(); //für nächsten gps fix vergleich
                        }
                    }
                } finally {
                    gpsFixTrack.unlockAfterRead();
                }
                if (currentFixTackType == lastTackType && currentFixTackType != null) {
                    addOrMergeTackTypeSegment(element, tackTypeSegments, gpsFixTrack, startOfSegment, end);
                }
            }
        }
        return tackTypeSegments;
    }

    private void addOrMergeTackTypeSegment(HasRaceOfCompetitorContext element,
            List<HasTackTypeSegmentContext> tackTypeSegments, final GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack,
            TimePoint startOfSegment, TimePoint endOfSegment) {
        if (tackTypeSegments.isEmpty() || settings.getMinimumDurationBetweenAdjacentTackTypeSegments() == null) {
            tackTypeSegments.add(createTackTypeSegment(startOfSegment, endOfSegment, element, gpsFixTrack));
        } else {
            final HasTackTypeSegmentContext previousSegment = tackTypeSegments.get(tackTypeSegments.size()-1);
            final TimePoint previousEnd = previousSegment.getEndOfTackTypeSegment();
            if (previousEnd.until(startOfSegment).compareTo(settings.getMinimumDurationBetweenAdjacentTackTypeSegments()) < 0) {
                // merge:
                tackTypeSegments.set(tackTypeSegments.size()-1, createTackTypeSegment(previousSegment.getStartOfTackTypeSegment(), endOfSegment, element, gpsFixTrack));
            } else {
                // add; duration between the segments is large enough
                tackTypeSegments.add(createTackTypeSegment(startOfSegment, endOfSegment, element, gpsFixTrack));
            }
        }
    }

    private HasTackTypeSegmentContext createTackTypeSegment(TimePoint startOfSegment, TimePoint endOfSegment,
            HasRaceOfCompetitorContext raceOfCompetitorContext, GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack) {
        return new TackTypeSegmentWithContext(new GPSFixTrackWithContext(raceOfCompetitorContext, gpsFixTrack), startOfSegment, endOfSegment); 
    }

}
