package com.sap.sailing.windestimation.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.maneuverdetection.TrackTimeInfo;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.windestimation.IncrementalWindEstimation;
import com.sap.sailing.domain.windestimation.TimePointAndPositionWithToleranceComparator;
import com.sap.sailing.domain.windestimation.WindTrackWithConfidenceForEachWindFixImpl;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.msthmm.DistanceAndDurationAwareWindTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.msthmm.MstBestPathsCalculator;
import com.sap.sailing.windestimation.aggregator.msthmm.MstBestPathsCalculatorImpl;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;
import com.sap.sailing.windestimation.data.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sailing.windestimation.windinference.DummyBasedTwsCalculatorImpl;
import com.sap.sailing.windestimation.windinference.MiddleCourseBasedTwdCalculatorImpl;
import com.sap.sailing.windestimation.windinference.PolarsBasedTwsCalculatorImpl;
import com.sap.sailing.windestimation.windinference.WindTrackCalculator;
import com.sap.sailing.windestimation.windinference.WindTrackCalculatorImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * Implementation of wind estimator which is meant to be assigned to a tracked race instance to provide a wind track
 * with estimated wind. Under the hood, it makes use of Minimum Spanning Tree based HMM which aggregates the maneuver
 * type classifications results such that a plausible wind track comes out. It operates incrementally, which means that
 * it maintains a state which is specific to the tracked race it is assigned to. The state is updated with each
 * {@link #newManeuverSpotsDetected(Competitor, Iterable, TrackTimeInfo)} call. The incremental state is managed in
 * {@link #mstManeuverGraphGenerator} which is responsible for incremental Minimum Spanning Tree computation for
 * provided maneuvers.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class IncrementalMstHmmWindEstimationForTrackedRace implements IncrementalWindEstimation {

    private static final double WIND_COURSE_TOLERANCE_IN_DEGREES_TO_IGNORE_FOR_REUSE = 1.0;
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;
    private final IncrementalMstManeuverGraphGenerator mstManeuverGraphGenerator;
    private final MstBestPathsCalculator bestPathsCalculator;
    private final WindTrackCalculator windTrackCalculator;
    private final Map<Pair<Position, TimePoint>, WindWithConfidence<Pair<Position, TimePoint>>> windTrackWithConfidences = new TreeMap<>(
            new TimePointAndPositionWithToleranceComparator());
    private final TrackedRace trackedRace;
    private final WindSource windSource;
    private final WindTrackWithConfidenceForEachWindFixImpl estimatedWindTrack;

    public IncrementalMstHmmWindEstimationForTrackedRace(TrackedRace trackedRace, WindSource windSource,
            PolarDataService polarDataService, long millisecondsOverWhichToAverage,
            ManeuverClassifiersCache maneuverClassifiersCache,
            GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache) {
        this.estimatedWindTrack = new WindTrackWithConfidenceForEachWindFixImpl(millisecondsOverWhichToAverage,
                DEFAULT_BASE_CONFIDENCE,
                WindSourceType.MANEUVER_BASED_ESTIMATION.useSpeed() && polarDataService != null,
                IncrementalMstHmmWindEstimationForTrackedRace.class.getName(), false, windTrackWithConfidences);
        this.trackedRace = trackedRace;
        this.windSource = windSource;
        DistanceAndDurationAwareWindTransitionProbabilitiesCalculator transitionProbabilitiesCalculator = new DistanceAndDurationAwareWindTransitionProbabilitiesCalculator(
                gaussianBasedTwdTransitionDistributionCache, true);
        this.mstManeuverGraphGenerator = new IncrementalMstManeuverGraphGenerator(
                new CompleteManeuverCurveToManeuverForEstimationConverter(trackedRace, polarDataService),
                transitionProbabilitiesCalculator, maneuverClassifiersCache);
        this.bestPathsCalculator = new MstBestPathsCalculatorImpl(transitionProbabilitiesCalculator);
        this.windTrackCalculator = new WindTrackCalculatorImpl(new MiddleCourseBasedTwdCalculatorImpl(),
                polarDataService == null ? new DummyBasedTwsCalculatorImpl()
                        : new PolarsBasedTwsCalculatorImpl(polarDataService));
    }

    @Override
    public WindTrack getWindTrack() {
        return estimatedWindTrack;
    }

    @Override
    public void newManeuverSpotsDetected(Competitor competitor, Iterable<CompleteManeuverCurve> newManeuvers,
            TrackTimeInfo trackTimeInfo) {
        List<ManeuverWithEstimatedType> maneuversWithEstimatedType = new ArrayList<>();
        estimatedWindTrack.lockForWrite();
        try {
            for (CompleteManeuverCurve newManeuverSpot : newManeuvers) {
                mstManeuverGraphGenerator.add(competitor, newManeuverSpot, trackTimeInfo);
            }
            MstManeuverGraphComponents graphComponents = mstManeuverGraphGenerator.parseGraph();
            if (graphComponents != null) {
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
                Map<Pair<Position, TimePoint>, WindWithConfidence<Pair<Position, TimePoint>>> newWindTrackMap = new HashMap<>(
                        newWindTrack.size());
                for (WindWithConfidence<Pair<Position, TimePoint>> wind : newWindTrack) {
                    newWindTrackMap.put(wind.getRelativeTo(), wind);
                }
                List<WindWithConfidence<Pair<Position, TimePoint>>> windFixesToAdd = new ArrayList<>();
                for (Iterator<WindWithConfidence<Pair<Position, TimePoint>>> previousWindFixesIterator = windTrackWithConfidences
                        .values().iterator(); previousWindFixesIterator.hasNext();) {
                    WindWithConfidence<Pair<Position, TimePoint>> previousWind = previousWindFixesIterator.next();
                    WindWithConfidence<Pair<Position, TimePoint>> newWind = newWindTrackMap
                            .get(previousWind.getRelativeTo());
                    if (newWind == null) {
                        previousWindFixesIterator.remove();
                        trackedRace.removeWind(previousWind.getObject(), windSource);
                    } else if (!isWindNearlySame(newWind.getObject(), previousWind.getObject())) {
                        previousWindFixesIterator.remove();
                        trackedRace.removeWind(previousWind.getObject(), windSource);
                        windFixesToAdd.add(newWind);
                    }
                }
                for (WindWithConfidence<Pair<Position, TimePoint>> newWind : newWindTrack) {
                    if (!windTrackWithConfidences.containsKey(newWind.getRelativeTo())) {
                        windFixesToAdd.add(newWind);
                    }
                }
                for (WindWithConfidence<Pair<Position, TimePoint>> windFixToAdd : windFixesToAdd) {
                    windTrackWithConfidences.put(windFixToAdd.getRelativeTo(), windFixToAdd);
                    trackedRace.recordWind(windFixToAdd.getObject(), windSource, false);
                }
            }
        } finally {
            estimatedWindTrack.unlockAfterWrite();
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
    public WindSource getWindSource() {
        return windSource;
    }

}
