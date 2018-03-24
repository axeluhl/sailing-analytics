package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.ManeuverWithContext;
import com.sap.sailing.datamining.shared.ManeuverSettings;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.Duration;
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
                ManeuverWithContext previousManeuverWithContext = null;
                ManeuverWithContext maneuverWithContextToAdd = null;
                for (Maneuver maneuver : maneuvers) {
                    ManeuverWithContext maneuverWithContext = new ManeuverWithContext(element, maneuver,
                            settings.isMainCurveAnalysis());

                    if (maneuverWithContextToAdd != null && !(settings.getTypeOfFollowingManeuver() != null
                            && !settings.getTypeOfFollowingManeuver()
                                    .contains(maneuverWithContext.getManeuver().getType())
                            || settings.getMinDurationToFollowingManeuver() != null
                                    && maneuverWithContextToAdd.getTimePointAfterForAnalysis()
                                            .until(maneuverWithContext.getTimePointBeforeForAnalysis())
                                            .compareTo(settings.getMinDurationToFollowingManeuver()) < 0
                            || settings.getMaxDurationToFollowingManeuver() != null
                                    && maneuverWithContextToAdd.getTimePointAfterForAnalysis()
                                            .until(maneuverWithContext.getTimePointBeforeForAnalysis())
                                            .compareTo(settings.getMaxDurationToFollowingManeuver()) > 0)) {
                        maneuversWithContext.add(maneuverWithContextToAdd);
                    }
                    // Compute only numbers which are really required for filtering
                    Duration maneuverDuration = settings.getMinManeuverDuration() != null
                            || settings.getMaxManeuverDuration() != null
                                    ? maneuverWithContext.getTimePointBeforeForAnalysis()
                                            .until(maneuverWithContext.getTimePointAfterForAnalysis())
                                    : null;
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
                            && maneuverDuration.compareTo(settings.getMinManeuverDuration()) < 0
                            || settings.getMaxManeuverDuration() != null
                                    && maneuverDuration.compareTo(settings.getMaxManeuverDuration()) > 0
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
                                    && maneuverExitingAbsTWA > settings.getMaxManeuverExitingAbsTWA()
                            || settings.getMinAbsCourseChangeInDegrees() != null && maneuverWithContext
                                    .getAbsoluteDirectionChangeInDegrees() < settings.getMinAbsCourseChangeInDegrees()
                            || settings.getMaxAbsCourseChangeInDegrees() != null && maneuverWithContext
                                    .getAbsoluteDirectionChangeInDegrees() > settings.getMaxAbsCourseChangeInDegrees()
                            || settings.getTypeOfPrecedingManeuver() != null
                                    && (previousManeuverWithContext == null || !settings.getTypeOfPrecedingManeuver()
                                            .contains(previousManeuverWithContext.getManeuver().getType()))
                            || settings.getMinDurationFromPrecedingManeuver() != null
                                    && (previousManeuverWithContext == null
                                            || previousManeuverWithContext.getTimePointAfterForAnalysis()
                                                    .until(maneuverWithContext.getTimePointBeforeForAnalysis())
                                                    .compareTo(settings.getMinDurationFromPrecedingManeuver()) < 0)
                            || settings.getMaxDurationFromPrecedingManeuver() != null
                                    && (previousManeuverWithContext == null
                                            || previousManeuverWithContext.getTimePointAfterForAnalysis()
                                                    .until(maneuverWithContext.getTimePointBeforeForAnalysis())
                                                    .compareTo(settings.getMaxDurationFromPrecedingManeuver()) > 0))) {

                        maneuverWithContextToAdd = maneuverWithContext;
                    } else {
                        maneuverWithContextToAdd = null;
                    }
                    previousManeuverWithContext = maneuverWithContext;
                }
                if (maneuverWithContextToAdd != null && settings.getTypeOfFollowingManeuver() == null
                        && settings.getMinDurationToFollowingManeuver() == null
                        && settings.getMaxDurationToFollowingManeuver() == null) {
                    maneuversWithContext.add(maneuverWithContextToAdd);
                }
            } catch (NoWindException e) {
                throw new IllegalStateException("No wind retrieving the maneuvers", e);
            }
        }
        return maneuversWithContext;
    }

}
