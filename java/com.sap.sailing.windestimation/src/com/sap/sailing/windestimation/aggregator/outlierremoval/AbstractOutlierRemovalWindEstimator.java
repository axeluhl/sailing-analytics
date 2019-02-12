package com.sap.sailing.windestimation.aggregator.outlierremoval;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.ManeuverClassificationsAggregator;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.util.ManeuverUtil;
import com.sap.sailing.windestimation.windinference.TwdFromManeuverCalculator;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Util.Pair;

public abstract class AbstractOutlierRemovalWindEstimator implements ManeuverClassificationsAggregator {

    public static final double MAX_DEVIATON_FROM_AVG_WIND_COURSE = 30;
    private final TwdFromManeuverCalculator twdCalculator;

    public AbstractOutlierRemovalWindEstimator(TwdFromManeuverCalculator twdCalculator) {
        this.twdCalculator = twdCalculator;
    }

    @Override
    public List<ManeuverWithEstimatedType> aggregateManeuverClassifications(
            RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications) {
        List<Pair<Bearing, ManeuverWithEstimatedType>> twdWithManeuvers = extractUsefulManeuversWithTwd(
                raceWithManeuverClassifications.getCompetitorTracks());
        OutlierAnalysisResult outlierAnalysisResult = analyzeOutlier(twdWithManeuvers);
        List<ManeuverWithEstimatedType> result = ManeuverUtil.getManeuverWithEstimatedTypeWithFixedConfidence(
                outlierAnalysisResult.getIncludedManeuvers(), outlierAnalysisResult.getFinalConfidence());
        return result;
    }

    protected abstract OutlierAnalysisResult analyzeOutlier(
            List<Pair<Bearing, ManeuverWithEstimatedType>> twdsWithManeuvers);

    private List<Pair<Bearing, ManeuverWithEstimatedType>> extractUsefulManeuversWithTwd(
            List<CompetitorTrackWithEstimationData<ManeuverWithProbabilisticTypeClassification>> competitorTracks) {
        List<Pair<Bearing, ManeuverWithEstimatedType>> twdsWithManeuvers = new ArrayList<>();
        for (CompetitorTrackWithEstimationData<ManeuverWithProbabilisticTypeClassification> competitorTrack : competitorTracks) {
            for (ManeuverWithProbabilisticTypeClassification estimationResult : competitorTrack.getElements()) {
                double highestLikelihood = 0;
                ManeuverTypeForClassification maneuverTypeWithHighestLikelihood = null;
                for (ManeuverTypeForClassification maneuverType : ManeuverTypeForClassification.values()) {
                    double likelihood = estimationResult.getManeuverTypeLikelihood(maneuverType);
                    if (highestLikelihood < likelihood) {
                        highestLikelihood = likelihood;
                        maneuverTypeWithHighestLikelihood = maneuverType;
                    }
                }
                if (maneuverTypeWithHighestLikelihood == ManeuverTypeForClassification.TACK
                        || maneuverTypeWithHighestLikelihood == ManeuverTypeForClassification.JIBE) {
                    ManeuverWithEstimatedType maneuverWithEstimatedType = new ManeuverWithEstimatedType(
                            estimationResult.getManeuver(), maneuverTypeWithHighestLikelihood, highestLikelihood);
                    Bearing twd = twdCalculator.getTwd(maneuverWithEstimatedType);
                    twdsWithManeuvers.add(new Pair<>(twd, maneuverWithEstimatedType));
                }
            }
        }
        return twdsWithManeuvers;
    }

}
