package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasCompetitorPolarContext;
import com.sap.sailing.polars.datamining.data.HasGPSFixPolarContext;
import com.sap.sailing.polars.datamining.data.impl.GPSFixWithPolarContext;
import com.sap.sailing.polars.datamining.data.impl.SpeedClusterGroup;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class PolarGPSFixRetrievalProcessor extends AbstractRetrievalProcessor<HasCompetitorPolarContext, HasGPSFixPolarContext> {

    public PolarGPSFixRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasGPSFixPolarContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasCompetitorPolarContext.class, HasGPSFixPolarContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasGPSFixPolarContext> retrieveData(HasCompetitorPolarContext element) {
        PolarSheetGenerationSettings settings = (PolarSheetGenerationSettings) getSettings();
        ClusterGroup<Speed> windSpeedRangeGroup = toClusterGroup(settings.getWindSpeedStepping());
        TrackedRace trackedRace = element.getTrackedRace();
        Competitor competitor = element.getCompetitor();
        Leg leg = element.getLeg();
        GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, leg);
        TimePoint startTime = trackedLeg.getStartTime();
        TimePoint finishTime = trackedLeg.getFinishTime();
        Set<HasGPSFixPolarContext> result = new HashSet<>();
        if (startTime != null && finishTime != null) {
            track.lockForRead();

            try {
                Iterable<GPSFixMoving> fixes = track.getFixes(startTime, true, finishTime, false);

                for (GPSFixMoving fix : fixes) {
                    result.add(new GPSFixWithPolarContext(fix, trackedRace, windSpeedRangeGroup, competitor, settings, element));
                }
            } finally {
                track.unlockAfterRead();
            }
        }
        return result;
    }

    private ClusterGroup<Speed> toClusterGroup(WindSpeedSteppingWithMaxDistance windSpeedStepping) {
        return SpeedClusterGroup.createSpeedClusterGroupFrom(windSpeedStepping);
    }

}
