package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.ManeuverWithContext;
import com.sap.sailing.datamining.impl.data.TrackedLegOfCompetitorWithSpecificTimePointWithContext;
import com.sap.sailing.datamining.shared.ManeuverSettings;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class ManeuverRetrievalProcessor
        extends AbstractRetrievalProcessor<HasTrackedLegOfCompetitorContext, HasManeuverContext> {

    private final ManeuverSettings settings;

    public ManeuverRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasManeuverContext, ?>> resultReceivers, ManeuverSettings settings,
            int retrievalLevel) {
        super(HasTrackedLegOfCompetitorContext.class, HasManeuverContext.class, executor, resultReceivers,
                retrievalLevel);
        this.settings = settings;
    }

    @Override
    protected Iterable<HasManeuverContext> retrieveData(HasTrackedLegOfCompetitorContext element) {
        Collection<HasManeuverContext> maneuversWithContext = new ArrayList<>();
        TimePoint finishTime = element.getTrackedLegOfCompetitor().getFinishTime();
        if (finishTime != null) {
            try {
                Iterable<Maneuver> maneuvers = element.getTrackedLegOfCompetitor().getManeuvers(finishTime, false);
                for (Maneuver maneuver : maneuvers) {
                    ManeuverWithContext maneuverWithContext = new ManeuverWithContext(new TrackedLegOfCompetitorWithSpecificTimePointWithContext(
                            element.getTrackedLegContext(), element.getTrackedLegOfCompetitor(), maneuver.getTimePoint()), maneuver, settings.isMainCurveAnalysis());
                    // Compute only numbers which are really required for filtering
                    double maneuverDuration = settings.getMinManeuverDuration() != null
                            || settings.getMaxManeuverDuration() != null ? maneuverWithContext.getManeuverDurationInSeconds()
                                    : 0;
                    double maneuverEnteringSpeed = settings.getMinManeuverEnteringSpeedInKnots() != null
                            || settings.getMaxManeuverEnteringSpeedInKnots() != null
                                    ? maneuverWithContext.getManeuverEnteringSpeed() : 0;
                    double maneuverExitingSpeed = settings.getMinManeuverExitingSpeedInKnots() != null
                            || settings.getMaxManeuverExitingSpeedInKnots() != null
                                    ? maneuverWithContext.getManeuverExitingSpeed() : 0;
                    double maneuverEnteringAbsTWA = settings.getMinManeuverEnteringAbsTWA() != null
                            ? maneuverWithContext.getEnteringAbsTWA() : 0;
                    double maneuverExitingAbsTWA = settings.getMinManeuverExitingAbsTWA() != null
                            ? maneuverWithContext.getExitingAbsTWA() : 0;
                    if (!(settings.getMinManeuverDuration() != null
                            && maneuverDuration < settings.getMinManeuverDuration()
                            || settings.getMaxManeuverDuration() != null
                                    && maneuverDuration > settings.getMaxManeuverDuration()
                            || settings.getMinManeuverEnteringSpeedInKnots() != null
                                    && maneuverEnteringSpeed < settings.getMinManeuverEnteringSpeedInKnots()
                            || settings.getMaxManeuverEnteringSpeedInKnots() != null
                                    && maneuverEnteringSpeed > settings.getMaxManeuverEnteringSpeedInKnots()
                            || settings.getMinManeuverExitingSpeedInKnots() != null
                                    && maneuverExitingSpeed < settings.getMinManeuverExitingSpeedInKnots()
                            || settings.getMaxManeuverExitingSpeedInKnots() != null
                                    && maneuverExitingSpeed > settings.getMaxManeuverExitingSpeedInKnots()
                            || settings.getMinManeuverEnteringAbsTWA() != null
                                    && maneuverEnteringAbsTWA < settings.getMinManeuverEnteringAbsTWA()
                            || settings.getMaxManeuverEnteringAbsTWA() != null
                                    && maneuverEnteringAbsTWA > settings.getMaxManeuverEnteringAbsTWA()
                            || settings.getMinManeuverExitingAbsTWA() != null
                                    && maneuverExitingAbsTWA < settings.getMinManeuverExitingAbsTWA()
                            || settings.getMaxManeuverExitingAbsTWA() != null
                                    && maneuverExitingAbsTWA > settings.getMaxManeuverExitingAbsTWA())) {
                        maneuversWithContext.add(maneuverWithContext);
                    }
                }
            } catch (NoWindException e) {
                throw new IllegalStateException("No wind retrieving the maneuvers", e);
            }
        }
        return maneuversWithContext;
    }

}
