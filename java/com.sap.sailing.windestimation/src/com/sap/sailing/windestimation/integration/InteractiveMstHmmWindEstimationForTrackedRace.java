package com.sap.sailing.windestimation.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.maneuverdetection.impl.TrackTimeInfo;
import com.sap.sailing.domain.maneuverdetection.impl.WindEstimationInteraction;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.msthmm.DistanceAndDurationAwareWindTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.msthmm.MstBestPathsCalculator;
import com.sap.sailing.windestimation.aggregator.msthmm.MstBestPathsCalculatorImpl;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sailing.windestimation.windinference.DummyBasedTwsCalculatorImpl;
import com.sap.sailing.windestimation.windinference.MiddleCourseBasedTwdCalculatorImpl;
import com.sap.sailing.windestimation.windinference.PolarsBasedTwsCalculatorImpl;
import com.sap.sailing.windestimation.windinference.WindTrackCalculator;
import com.sap.sailing.windestimation.windinference.WindTrackCalculatorImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class InteractiveMstHmmWindEstimationForTrackedRace extends WindTrackImpl implements WindEstimationInteraction {

    private static final long serialVersionUID = 6654969432545619955L;
    private static final double WIND_COURSE_TOLERANCE_IN_DEGREES_TO_IGNORE_FOR_REUSE = 1.0;
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;
    private final IncrementalMstManeuverGraphGenerator mstManeuverGraphGenerator;
    private final MstBestPathsCalculator bestPathsCalculator;
    private final WindTrackCalculator windTrackCalculator;
    private final Map<Wind, WindWithConfidence<Pair<Position, TimePoint>>> windTrackWithConfidences = new HashMap<>();
    private final TrackedRace trackedRace;
    private WindSource windSource;

    public InteractiveMstHmmWindEstimationForTrackedRace(TrackedRace trackedRace, WindSource windSource,
            long millisecondsOverWhichToAverage) {
        this(trackedRace, windSource, trackedRace.getPolarDataService(), millisecondsOverWhichToAverage,
                DefaultModelCaches.MANEUVER_CLASSIFIERS_CACHE,
                DefaultModelCaches.GAUSSIAN_TWD_DELTA_TRANSITION_DISTRIBUTION_CACHE);
    }

    public InteractiveMstHmmWindEstimationForTrackedRace(TrackedRace trackedRace, WindSource windSource,
            PolarDataService polarDataService, long millisecondsOverWhichToAverage,
            ManeuverClassifiersCache maneuverClassifiersCache,
            GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache) {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE,
                WindSourceType.MANEUVER_BASED_ESTIMATION.useSpeed() && polarDataService != null,
                InteractiveMstHmmWindEstimationForTrackedRace.class.getName());
        this.trackedRace = trackedRace;
        this.windSource = windSource;
        DistanceAndDurationAwareWindTransitionProbabilitiesCalculator transitionProbabilitiesCalculator = new DistanceAndDurationAwareWindTransitionProbabilitiesCalculator(
                gaussianBasedTwdTransitionDistributionCache, true);
        this.mstManeuverGraphGenerator = new IncrementalMstManeuverGraphGenerator(trackedRace,
                transitionProbabilitiesCalculator, maneuverClassifiersCache, polarDataService);
        this.bestPathsCalculator = new MstBestPathsCalculatorImpl(transitionProbabilitiesCalculator);
        this.windTrackCalculator = new WindTrackCalculatorImpl(new MiddleCourseBasedTwdCalculatorImpl(),
                polarDataService == null ? new DummyBasedTwsCalculatorImpl()
                        : new PolarsBasedTwsCalculatorImpl(polarDataService));
    }

    @Override
    public void newManeuverSpotsDetected(Competitor competitor, Iterable<CompleteManeuverCurve> newManeuvers,
            TrackTimeInfo trackTimeInfo) {
        List<ManeuverWithEstimatedType> maneuversWithEstimatedType = new ArrayList<>();
        lockForWrite();
        try {
            for (CompleteManeuverCurve newManeuverSpot : newManeuvers) {
                mstManeuverGraphGenerator.add(competitor, newManeuverSpot, trackTimeInfo);
            }
            MstManeuverGraphComponents graphComponents = mstManeuverGraphGenerator.parseGraph();
            List<GraphLevelInference> bestPath = bestPathsCalculator.getBestNodes(graphComponents);
            for (GraphLevelInference inference : bestPath) {
                ManeuverWithEstimatedType maneuverWithEstimatedType = new ManeuverWithEstimatedType(
                        inference.getGraphLevel().getManeuver(), inference.getGraphNode().getManeuverType(),
                        inference.getConfidence());
                maneuversWithEstimatedType.add(maneuverWithEstimatedType);
            }
            Collections.sort(maneuversWithEstimatedType);
            List<WindWithConfidence<Pair<Position, TimePoint>>> newWindTrack = windTrackCalculator
                    .getWindTrackFromManeuverClassifications(maneuversWithEstimatedType);
            Map<Wind, WindWithConfidence<Pair<Position, TimePoint>>> newWindTrackMap = new HashMap<>(
                    newWindTrack.size());
            for (WindWithConfidence<Pair<Position, TimePoint>> wind : newWindTrack) {
                newWindTrackMap.put(wind.getObject(), wind);
            }
            for (WindWithConfidence<Pair<Position, TimePoint>> previousWind : windTrackWithConfidences.values()) {
                WindWithConfidence<Pair<Position, TimePoint>> newWind = newWindTrackMap.get(previousWind.getObject());
                if (newWind == null) {
                    windTrackWithConfidences.remove(previousWind.getObject());
                    trackedRace.removeWind(previousWind.getObject(), windSource);
                } else if (!isWindNearlySame(newWind.getObject(), previousWind.getObject())) {
                    windTrackWithConfidences.put(newWind.getObject(), newWind);
                    trackedRace.removeWind(previousWind.getObject(), windSource);
                    trackedRace.recordWind(newWind.getObject(), windSource, false);
                }
            }
            for (WindWithConfidence<Pair<Position, TimePoint>> newWind : newWindTrack) {
                if (!windTrackWithConfidences.containsKey(newWind.getObject())) {
                    windTrackWithConfidences.put(newWind.getObject(), newWind);
                    trackedRace.recordWind(newWind.getObject(), windSource, false);
                }
            }
        } finally {
            unlockAfterWrite();
        }
    }

    private boolean isWindNearlySame(Wind oneWind, Wind otherWind) {
        double bearingInDegrees = oneWind.getBearing().getDifferenceTo(otherWind.getBearing()).abs().getDegrees();
        if (bearingInDegrees > WIND_COURSE_TOLERANCE_IN_DEGREES_TO_IGNORE_FOR_REUSE) {
            return false;
        }
        return true;
    }

    @Override
    protected double getConfidenceOfInternalWindFixUnsynchronized(Wind windFix) {
        WindWithConfidence<Pair<Position, TimePoint>> windWithConfidence = windTrackWithConfidences.get(windFix);
        return super.getConfidenceOfInternalWindFixUnsynchronized(windFix) * windWithConfidence.getConfidence();
    }

}
