package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.preprocessing.PreprocessingPipeline;
import com.sap.sailing.windestimation.windinference.WindTrackCalculator;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public interface WindEstimationComponentWithInternals<InputType> extends WindEstimationComponent<InputType> {

    List<WindWithConfidence<Pair<Position, TimePoint>>> estimateWindTrackAfterPreprocessing(
            RaceWithEstimationData<ManeuverForEstimation> race);

    List<WindWithConfidence<Pair<Position, TimePoint>>> estimateWindTrackAfterManeuverClassification(
            RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications);

    List<WindWithConfidence<Pair<Position, TimePoint>>> estimateWindTrackAfterManeuverClassificationsAggregation(
            List<ManeuverWithEstimatedType> improvedManeuverClassifications);

    PreprocessingPipeline<InputType, RaceWithEstimationData<ManeuverForEstimation>> getPreprocessingPipeline();

    ManeuverClassificationsAggregator getManeuverClassificationsAggregator();

    ManeuverClassifiersCache getManeuverClassifiersCache();

    WindTrackCalculator getWindTrackCalculator();

}
