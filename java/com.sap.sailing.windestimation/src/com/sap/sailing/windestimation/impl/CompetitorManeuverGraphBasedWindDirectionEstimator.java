package com.sap.sailing.windestimation.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.ManeuverBasedWindDirectionEstimator;
import com.sap.sailing.windestimation.impl.maneuvergraph.ManeuverSequenceGraph;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompetitorManeuverGraphBasedWindDirectionEstimator implements ManeuverBasedWindDirectionEstimator {

    private final PolarDataService polarService;

    public CompetitorManeuverGraphBasedWindDirectionEstimator(PolarDataService polarService) {
        this.polarService = polarService;
    }

    @Override
    public Iterable<WindTrackCandidate> computeWindTrackCandidates(BoatClass boatClass,
            Iterable<CompleteManeuverCurveWithEstimationData> competitorManeuvers) {
        ManeuverSequenceGraph maneuverSequenceGraph = new ManeuverSequenceGraph(boatClass, polarService,
                competitorManeuvers);
        maneuverSequenceGraph.computePossiblePathsWithDistances();
        return maneuverSequenceGraph.computeWindDirectionCandidates();
    }

}
