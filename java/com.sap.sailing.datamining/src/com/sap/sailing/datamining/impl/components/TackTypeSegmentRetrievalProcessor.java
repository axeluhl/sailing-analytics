package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasTackTypeSegmentContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.BravoFixTrackWithContext;
import com.sap.sailing.datamining.impl.data.TackTypeSegmentWithContext;
import com.sap.sailing.datamining.shared.TackTypeSegmentsDataMiningSettings;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.tracking.BravoFixTrack;
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
            final BravoFixTrack<Competitor> bravoFixTrack = trackedRace.getSensorTrack(element.getCompetitor(), BravoFixTrack.TRACK_NAME);
            if (bravoFixTrack != null) {
                boolean isTackType = false;
                TimePoint last = null;
                TimePoint startOfSegment = null;
                bravoFixTrack.lockForRead();
                try {
                    for (final BravoFix bravoFix : bravoFixTrack.getFixes(startOfRace, /* fromInclusive */ true, end, /* toInclusive */ false)) {
                        if (isAborted()) {
                            break;
                        }
                        TrackedLegOfCompetitor trackedLegComp = element.getTrackedRaceContext().getTrackedRace().getTrackedLeg(element.getCompetitor(), bravoFix.getTimePoint());
                        boolean currentFixIsTackType;
                        try {
                            currentFixIsTackType = (bravoFix.isTackType(trackedLegComp.getTackType(bravoFix.getTimePoint())));
                        } catch (NoWindException e) {
                            currentFixIsTackType=false;
                        }
                        if (currentFixIsTackType != isTackType) {
                            if (currentFixIsTackType) {
                                startOfSegment = bravoFix.getTimePoint();
                            } else {
                                if (settings.getMinimumTackTypeSegmentDuration() == null ||
                                        startOfSegment.until(last).compareTo(settings.getMinimumTackTypeSegmentDuration()) >= 0) {
                                    addOrMergeTackTypeSegment(element, tackTypeSegments, bravoFixTrack, startOfSegment,
                                            last /* don't include the last interval ending at the non-TackType fix */);
                                }
                                startOfSegment = null;
                            }
                            isTackType = currentFixIsTackType;
                        }
                        last = bravoFix.getTimePoint();
                    }
                } finally {
                    bravoFixTrack.unlockAfterRead();
                }
                if (isTackType) {
                    addOrMergeTackTypeSegment(element, tackTypeSegments, bravoFixTrack, startOfSegment, end);
                }
            }
        }
        return tackTypeSegments;
    }

    private void addOrMergeTackTypeSegment(HasRaceOfCompetitorContext element,
            List<HasTackTypeSegmentContext> tackTypeSegments, final BravoFixTrack<Competitor> bravoFixTrack,
            TimePoint startOfSegment, TimePoint endOfSegment) {
        if (tackTypeSegments.isEmpty() || settings.getMinimumDurationBetweenAdjacentTackTypeSegments() == null) {
            tackTypeSegments.add(createTackTypeSegment(startOfSegment, endOfSegment, element, bravoFixTrack));
        } else {
            final HasTackTypeSegmentContext previousSegment = tackTypeSegments.get(tackTypeSegments.size()-1);
            final TimePoint previousEnd = previousSegment.getEndOfTackTypeSegment();
            if (previousEnd.until(startOfSegment).compareTo(settings.getMinimumDurationBetweenAdjacentTackTypeSegments()) < 0) {
                // merge:
                tackTypeSegments.set(tackTypeSegments.size()-1, createTackTypeSegment(previousSegment.getStartOfTackTypeSegment(), endOfSegment, element, bravoFixTrack));
            } else {
                // add; duration between the segments is large enough
                tackTypeSegments.add(createTackTypeSegment(startOfSegment, endOfSegment, element, bravoFixTrack));
            }
        }
    }

    private HasTackTypeSegmentContext createTackTypeSegment(TimePoint startOfSegment, TimePoint endOfSegment,
            HasRaceOfCompetitorContext raceOfCompetitorContext, BravoFixTrack<Competitor> bravoFixTrack) {
        return new TackTypeSegmentWithContext(new BravoFixTrackWithContext(raceOfCompetitorContext, bravoFixTrack), startOfSegment, endOfSegment); 
    }

}
