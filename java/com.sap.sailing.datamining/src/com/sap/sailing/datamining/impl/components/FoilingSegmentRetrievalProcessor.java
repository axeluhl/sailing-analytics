package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasFoilingSegmentContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.BravoFixTrackWithContext;
import com.sap.sailing.datamining.impl.data.FoilingSegmentWithContext;
import com.sap.sailing.datamining.shared.FoilingSegmentsDataMiningSettings;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class FoilingSegmentRetrievalProcessor extends AbstractRetrievalProcessor<HasRaceOfCompetitorContext, HasFoilingSegmentContext> {
    private final FoilingSegmentsDataMiningSettings settings;

    public FoilingSegmentRetrievalProcessor(ExecutorService executor, Collection<Processor<HasFoilingSegmentContext, ?>> resultReceivers,
            FoilingSegmentsDataMiningSettings settings, int retrievalLevel) {
        super(HasRaceOfCompetitorContext.class, HasFoilingSegmentContext.class, executor, resultReceivers, retrievalLevel);
        this.settings = settings;
    }

    @Override
    protected Iterable<HasFoilingSegmentContext> retrieveData(HasRaceOfCompetitorContext element) {
        List<HasFoilingSegmentContext> foilingSegments = new ArrayList<>();
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
                boolean isFoiling = false;
                TimePoint last = null;
                TimePoint startOfSegment = null;
                bravoFixTrack.lockForRead();
                try {
                    for (final BravoFix bravoFix : bravoFixTrack.getFixes(startOfRace, /* fromInclusive */ true, end, /* toInclusive */ false)) {
                        if (isAborted()) {
                            break;
                        }
                        final boolean currentFixIsFoiling = 
                                (bravoFix.isFoiling(settings.getMinimumRideHeight()) &&
                                        (settings.getMinimumSpeedForFoiling() == null || settings.getMinimumSpeedForFoiling().compareTo(
                                                element.getTrackedRaceContext().getTrackedRace().getTrack(element.getCompetitor()).getEstimatedSpeed(bravoFix.getTimePoint())) <= 0)) ||
                                (settings.getMaximumSpeedNotFoiling() != null && settings.getMaximumSpeedNotFoiling().compareTo(
                                        element.getTrackedRaceContext().getTrackedRace().getTrack(element.getCompetitor()).getEstimatedSpeed(bravoFix.getTimePoint())) <= 0);
                        if (currentFixIsFoiling != isFoiling) {
                            if (currentFixIsFoiling) {
                                startOfSegment = bravoFix.getTimePoint();
                            } else {
                                if (settings.getMinimumFoilingSegmentDuration() == null ||
                                        startOfSegment.until(last).compareTo(settings.getMinimumFoilingSegmentDuration()) >= 0) {
                                    addOrMergeFoilingSegment(element, foilingSegments, bravoFixTrack, startOfSegment,
                                            last /* don't include the last interval ending at the non-foiling fix */);
                                }
                                startOfSegment = null;
                            }
                            isFoiling = currentFixIsFoiling;
                        }
                        last = bravoFix.getTimePoint();
                    }
                } finally {
                    bravoFixTrack.unlockAfterRead();
                }
                if (isFoiling) {
                    addOrMergeFoilingSegment(element, foilingSegments, bravoFixTrack, startOfSegment, end);
                }
            }
        }
        return foilingSegments;
    }

    private void addOrMergeFoilingSegment(HasRaceOfCompetitorContext element,
            List<HasFoilingSegmentContext> foilingSegments, final BravoFixTrack<Competitor> bravoFixTrack,
            TimePoint startOfSegment, TimePoint endOfSegment) {
        if (foilingSegments.isEmpty() || settings.getMinimumDurationBetweenAdjacentFoilingSegments() == null) {
            foilingSegments.add(createFoilingSegment(startOfSegment, endOfSegment, element, bravoFixTrack));
        } else {
            final HasFoilingSegmentContext previousSegment = foilingSegments.get(foilingSegments.size()-1);
            final TimePoint previousEnd = previousSegment.getEndOfFoilingSegment();
            if (previousEnd.until(startOfSegment).compareTo(settings.getMinimumDurationBetweenAdjacentFoilingSegments()) < 0) {
                // merge:
                foilingSegments.set(foilingSegments.size()-1, createFoilingSegment(previousSegment.getStartOfFoilingSegment(), endOfSegment, element, bravoFixTrack));
            } else {
                // add; duration between the segments is large enough
                foilingSegments.add(createFoilingSegment(startOfSegment, endOfSegment, element, bravoFixTrack));
            }
        }
    }

    private HasFoilingSegmentContext createFoilingSegment(TimePoint startOfSegment, TimePoint endOfSegment,
            HasRaceOfCompetitorContext raceOfCompetitorContext, BravoFixTrack<Competitor> bravoFixTrack) {
        return new FoilingSegmentWithContext(new BravoFixTrackWithContext(raceOfCompetitorContext, bravoFixTrack), startOfSegment, endOfSegment); 
    }

}
