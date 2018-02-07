package com.sap.sailing.windestimation.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.windestimation.ManeuverBasedWindDirectionEstimator;
import com.sap.sailing.windestimation.impl.maneuvergraph.ManeuverSequenceGraph;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompetitorManeuverGraphBasedWindDirectionEstimator implements ManeuverBasedWindDirectionEstimator {
    
    private final PolarDataService polarService;
    private final IManeuverSpeedRetriever maneuverSpeedRetriever;

    public CompetitorManeuverGraphBasedWindDirectionEstimator(PolarDataService polarService, IManeuverSpeedRetriever maneuverSpeedRetriever) {
        this.polarService = polarService;
        this.maneuverSpeedRetriever = maneuverSpeedRetriever;
    }

    @Override
    public Iterable<WindTrackCandidate> computeWindDirectionCandidates(BoatClass boatClass, Iterable<Maneuver> competitorManeuvers) {
        ManeuverSequenceGraph maneuverSequenceGraph = new ManeuverSequenceGraph(boatClass, polarService, maneuverSpeedRetriever, competitorManeuvers);
        maneuverSequenceGraph.computePossiblePathsWithDistances();
        return maneuverSequenceGraph.computeWindDirectionCandidates();
    }

}
