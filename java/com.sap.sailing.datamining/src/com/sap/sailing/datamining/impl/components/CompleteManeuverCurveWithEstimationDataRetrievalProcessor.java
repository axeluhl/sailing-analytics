package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasCompleteManeuverCurveWithEstimationDataContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.CompleteManeuverCurveWithEstimationDataWithContext;
import com.sap.sailing.datamining.shared.ManeuverSettings;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverDetectorWithEstimationDataSupport;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorWithEstimationDataSupportDecoratorImpl;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurveWithEstimationDataRetrievalProcessor extends
        AbstractRetrievalProcessor<HasRaceOfCompetitorContext, HasCompleteManeuverCurveWithEstimationDataContext> {

    private final ManeuverSettings settings;

    public CompleteManeuverCurveWithEstimationDataRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasCompleteManeuverCurveWithEstimationDataContext, ?>> resultReceivers,
            ManeuverSettings settings, int retrievalLevel, String retrievedDataTypeMessageKey) {
        super(HasRaceOfCompetitorContext.class, HasCompleteManeuverCurveWithEstimationDataContext.class, executor,
                resultReceivers, retrievalLevel, retrievedDataTypeMessageKey);
        this.settings = settings;
    }

    @Override
    protected Iterable<HasCompleteManeuverCurveWithEstimationDataContext> retrieveData(
            HasRaceOfCompetitorContext element) {
        List<HasCompleteManeuverCurveWithEstimationDataContext> result = new ArrayList<>();
        TrackedRace trackedRace = element.getTrackedRaceContext().getTrackedRace();
        Competitor competitor = element.getCompetitor();
        ManeuverDetectorWithEstimationDataSupport maneuverDetector = new ManeuverDetectorWithEstimationDataSupportDecoratorImpl(
                new ManeuverDetectorImpl(trackedRace, competitor),
                element.getTrackedRaceContext().getLeaderboardContext().getLeaderboardGroupContext().getPolarDataService());
        
        Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
        if (isAborted()) {
            return result;
        }
        
        Iterable<CompleteManeuverCurve> maneuverCurves = maneuverDetector.getCompleteManeuverCurves(maneuvers);
        if (isAborted()) {
            return result;
        }
        
        Iterable<CompleteManeuverCurveWithEstimationData> maneuversWithEstimationData = maneuverDetector
                .getCompleteManeuverCurvesWithEstimationData(maneuverCurves);
        CompleteManeuverCurveWithEstimationData previousManeuver = null;
        CompleteManeuverCurveWithEstimationData currentManeuver = null;
        for (CompleteManeuverCurveWithEstimationData nextManeuver : maneuversWithEstimationData) {
            if (isAborted()) {
                break;
            }
            
            if (currentManeuver != null) {
                CompleteManeuverCurveWithEstimationDataWithContext maneuverWithContext = new CompleteManeuverCurveWithEstimationDataWithContext(
                        element, currentManeuver, settings, previousManeuver, nextManeuver);
                if (currentManeuver.getWind() != null
                        && isManeuverCompliantWithSettings(previousManeuver, maneuverWithContext, nextManeuver)) {
                    result.add(maneuverWithContext);
                }
            }
            previousManeuver = currentManeuver;
            currentManeuver = nextManeuver;
        }
        if (currentManeuver != null) {
            CompleteManeuverCurveWithEstimationDataWithContext maneuverWithContext = new CompleteManeuverCurveWithEstimationDataWithContext(
                    element, currentManeuver, settings, previousManeuver, null);
            if (currentManeuver.getWind() != null
                    && isManeuverCompliantWithSettings(previousManeuver, maneuverWithContext, null)) {
                result.add(maneuverWithContext);
            }
        }
        return result;
    }

    private boolean isManeuverCompliantWithSettings(CompleteManeuverCurveWithEstimationData previousManeuver,
            CompleteManeuverCurveWithEstimationDataWithContext currentManeuverWithContext,
            CompleteManeuverCurveWithEstimationData nextManeuver) {
        boolean mainCurveAnalysis = settings.isMainCurveAnalysis();
        // Compute only numbers which are really required for filtering
        Duration maneuverDuration = settings.getMinManeuverDuration() != null
                || settings.getMaxManeuverDuration() != null
                        ? currentManeuverWithContext.getTimePointBeforeForAnalysis()
                                .until(currentManeuverWithContext.getTimePointAfterForAnalysis())
                        : null;
        double maneuverEnteringSpeed = settings.getMinManeuverEnteringSpeedInKnots() != null
                || settings.getMaxManeuverEnteringSpeedInKnots() != null
                        ? currentManeuverWithContext.getManeuverEnteringSpeed() : 0;
        double maneuverExitingSpeed = settings.getMinManeuverExitingSpeedInKnots() != null
                || settings.getMaxManeuverExitingSpeedInKnots() != null
                        ? currentManeuverWithContext.getManeuverExitingSpeed() : 0;
        double maneuverEnteringAbsTWA = settings.getMinManeuverEnteringAbsTWA() != null
                ? currentManeuverWithContext.getEnteringAbsTWA() : 0;
        double maneuverExitingAbsTWA = settings.getMinManeuverExitingAbsTWA() != null
                ? currentManeuverWithContext.getExitingAbsTWA() : 0;

        Duration durationToPreviousManeuver = previousManeuver != null
                ? getManeuverBoundariesForAnalysis(previousManeuver, mainCurveAnalysis).getTimePointAfter()
                        .until(currentManeuverWithContext.getTimePointBeforeForAnalysis())
                : null;
        Duration durationToNextManeuver = nextManeuver != null
                ? currentManeuverWithContext.getTimePointAfterForAnalysis().until(
                        getManeuverBoundariesForAnalysis(nextManeuver, mainCurveAnalysis).getTimePointBefore())
                : null;
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
                || settings.getMinAbsCourseChangeInDegrees() != null && currentManeuverWithContext
                        .getAbsoluteDirectionChangeInDegrees() < settings.getMinAbsCourseChangeInDegrees()
                || settings.getMaxAbsCourseChangeInDegrees() != null && currentManeuverWithContext
                        .getAbsoluteDirectionChangeInDegrees() > settings.getMaxAbsCourseChangeInDegrees()
                || settings.getMinDurationFromPrecedingManeuver() != null && (previousManeuver == null
                        || durationToPreviousManeuver.compareTo(settings.getMinDurationFromPrecedingManeuver()) < 0)
                || settings.getMaxDurationFromPrecedingManeuver() != null && (previousManeuver == null
                        || durationToPreviousManeuver.compareTo(settings.getMaxDurationFromPrecedingManeuver()) > 0)
                || settings.getMinDurationToFollowingManeuver() != null && (nextManeuver == null
                        || durationToNextManeuver.compareTo(settings.getMinDurationToFollowingManeuver()) < 0)
                || settings.getMaxDurationToFollowingManeuver() != null && (nextManeuver == null
                        || durationToNextManeuver.compareTo(settings.getMaxDurationToFollowingManeuver()) > 0))) {
            return true;
        } else {
            return false;
        }
    }

    private ManeuverCurveBoundaries getManeuverBoundariesForAnalysis(CompleteManeuverCurveWithEstimationData maneuver,
            boolean mainCurveAnalysis) {
        return mainCurveAnalysis ? maneuver.getMainCurve() : maneuver.getCurveWithUnstableCourseAndSpeed();
    }

}
