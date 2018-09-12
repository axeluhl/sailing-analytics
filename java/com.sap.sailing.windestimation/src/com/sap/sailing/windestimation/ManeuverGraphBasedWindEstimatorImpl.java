package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuvergraph.BestPathCalculatorWithPolars;
import com.sap.sailing.windestimation.maneuvergraph.BestPathsCalculator;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverSequenceGraph;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverGraphBasedWindEstimatorImpl extends AbstractManeuverForEstimationBasedWindEstimatorImpl {

    private final PolarDataService polarService;
    private final ManeuverFeatures maneuverFeatures;

    public ManeuverGraphBasedWindEstimatorImpl(PolarDataService polarService, ManeuverFeatures maneuverFeatures) {
        this.polarService = polarService;
        this.maneuverFeatures = maneuverFeatures;
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrackWithManeuvers(
            RaceWithEstimationData<ManeuverForEstimation> race) {
        BestPathsCalculator bestPathsCalculator = maneuverFeatures.isPolarsInformation()
                ? new BestPathCalculatorWithPolars(new PolarsFittingWindEstimation(polarService), false)
                : new BestPathsCalculator();
        ManeuverSequenceGraph maneuverGraph = new ManeuverSequenceGraph(race.getCompetitorTracks(),
                new ManeuverClassifiersCache(60000, maneuverFeatures, polarService), bestPathsCalculator);
        return maneuverGraph.estimateWindTrack();
    }
}
