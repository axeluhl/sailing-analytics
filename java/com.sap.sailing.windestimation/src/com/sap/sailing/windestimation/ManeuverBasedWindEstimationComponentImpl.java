package com.sap.sailing.windestimation;

import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.preprocessing.PreprocessingPipeline;
import com.sap.sailing.windestimation.windinference.WindTrackCalculator;;

public class ManeuverBasedWindEstimationComponentImpl<InputType>
        implements WindEstimationComponentWithInternals<InputType> {
    private final PreprocessingPipeline<InputType, RaceWithEstimationData<ManeuverForEstimation>> preprocessingPipeline;
    private final ManeuverClassifiersCache maneuverClassifiersCache;
    private final ManeuverClassificationsAggregator maneuverClassificationsAggregator;
    private final WindTrackCalculator windTrackCalculator;

    public ManeuverBasedWindEstimationComponentImpl(
            PreprocessingPipeline<InputType, RaceWithEstimationData<ManeuverForEstimation>> preprocessingPipeline,
            ManeuverClassifiersCache maneuverClassifiersCache,
            ManeuverClassificationsAggregator maneuverClassificationsAggregator,
            WindTrackCalculator windTrackCalculator) {
        this.preprocessingPipeline = preprocessingPipeline;
        this.maneuverClassifiersCache = maneuverClassifiersCache;
        this.maneuverClassificationsAggregator = maneuverClassificationsAggregator;
        this.windTrackCalculator = windTrackCalculator;
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrack(InputType input) {
        RaceWithEstimationData<ManeuverForEstimation> race = preprocessingPipeline.preprocessRace(input);
        return estimateWindTrackAfterPreprocessing(race);
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrackAfterPreprocessing(
            RaceWithEstimationData<ManeuverForEstimation> race) {
        List<CompetitorTrackWithEstimationData<ManeuverWithProbabilisticTypeClassification>> competitorTracks = race
                .getCompetitorTracks().stream().map(competitorTrack -> {
                    List<ManeuverWithProbabilisticTypeClassification> maneuverClassifications = competitorTrack
                            .getElements().stream().map(maneuver -> maneuverClassifiersCache.getBestClassifier(maneuver)
                                    .classifyManeuver(maneuver))
                            .collect(Collectors.toList());
                    return competitorTrack.constructWithElements(maneuverClassifications);
                }).collect(Collectors.toList());
        RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications = race
                .constructWithElements(competitorTracks);
        return estimateWindTrackAfterManeuverClassification(raceWithManeuverClassifications);
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrackAfterManeuverClassification(
            RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications) {
        List<ManeuverWithEstimatedType> improvedManeuverClassifications = maneuverClassificationsAggregator
                .aggregateManeuverClassifications(raceWithManeuverClassifications);
        return estimateWindTrackAfterManeuverClassificationsAggregation(improvedManeuverClassifications);
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrackAfterManeuverClassificationsAggregation(
            List<ManeuverWithEstimatedType> improvedManeuverClassifications) {
        List<WindWithConfidence<Void>> windTrack = windTrackCalculator
                .getWindTrackFromManeuverClassifications(improvedManeuverClassifications);
        return windTrack;
    }

    @Override
    public PreprocessingPipeline<InputType, RaceWithEstimationData<ManeuverForEstimation>> getPreprocessingPipeline() {
        return preprocessingPipeline;
    }

    @Override
    public ManeuverClassificationsAggregator getManeuverClassificationsAggregator() {
        return maneuverClassificationsAggregator;
    }

    @Override
    public ManeuverClassifiersCache getManeuverClassifiersCache() {
        return maneuverClassifiersCache;
    }

    @Override
    public WindTrackCalculator getWindTrackCalculator() {
        return windTrackCalculator;
    }
}
