package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuvergraph.BestPathCalculatorWithPolars;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverSequenceGraph;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverGraphBasedWindEstimatorImpl extends AbstractManeuverForEstimationBasedWindEstimatorImpl {

    private final PolarDataService polarService;

    public ManeuverGraphBasedWindEstimatorImpl(PolarDataService polarService) {
        this.polarService = polarService;
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrackWithManeuvers(
            RaceWithEstimationData<ManeuverForEstimation> race) {
        ManeuverSequenceGraph maneuverGraph = new ManeuverSequenceGraph(race.getCompetitorTracks(),
                new ManeuverClassifiersCache(60000, false, true, polarService),
                new BestPathCalculatorWithPolars(new PolarsFittingWindEstimation(polarService), false));
        return maneuverGraph.estimateWindTrack();
    }
}
