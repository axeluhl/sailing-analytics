package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.preprocessing.PreprocessingPipeline;
import com.sap.sailing.windestimation.windinference.WindTrackCalculator;

public interface WindEstimationComponentWithInternals<InputType> extends WindEstimationComponent<InputType> {

    List<WindWithConfidence<Void>> estimateWindTrackAfterPreprocessing(
            RaceWithEstimationData<ManeuverForEstimation> race);

    List<WindWithConfidence<Void>> estimateWindTrackAfterManeuverClassification(
            RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications);

    List<WindWithConfidence<Void>> estimateWindTrackAfterManeuverClassificationsAggregation(
            List<ManeuverWithEstimatedType> improvedManeuverClassifications);

    PreprocessingPipeline<InputType, RaceWithEstimationData<ManeuverForEstimation>> getPreprocessingPipeline();

    ManeuverClassificationsAggregator getManeuverClassificationsAggregator();

    ManeuverClassifiersCache getManeuverClassifiersCache();

    WindTrackCalculator getWindTrackCalculator();

}
