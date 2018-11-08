package com.sap.sailing.windestimation;

public class WindEstimationComponent<InputType> {
    /*
    private final PreprocessingPipeline<InputType, RaceWithEstimationData<ManeuverForEstimation>> preprocessingPipeline;
    private final ManeuverClassifiersCache maneuverClassifiersCache;
    private final ManeuverClassificationsAggregator maneuverClassificationsAggregator;
    private final ManeuverClassificationsWindTrackInferrer windTrackInferrer;

    public WindEstimationComponent(PreprocessingPipeline<InputType, RaceWithEstimationData<ManeuverForEstimation>> preprocessingPipeline, boolean racePerCompetitorTrack, ManeuverClassifiersCache maneuverClassifiersCache, ManeuverClassificationsAggregator maneuverClassificationsAggregator, ManeuverClassificationsWindTrackInferrer windTrackInferrer) {
        this.preprocessingPipeline = preprocessingPipeline;
        this.maneuverClassifiersCache = maneuverClassifiersCache;
        this.maneuverClassificationsAggregator = maneuverClassificationsAggregator;
        this.windTrackInferrer = windTrackInferrer;
        
    }
    
    public List<WindWithConfidence<Void>> estimateWindTrack(InputType input) {
        RaceWithEstimationData<ManeuverForEstimation> race = preprocessingPipeline.preprocessRace(input);
        List<ManeuverClassification> maneuverClassifications = maneuvers.stream().map(maneuver -> maneuverClassifiersCache.getBestClassifier(maneuver).classifyManeuver(maneuver)).collect(Collectors.toList());
        List<ManeuverClassification> improvedManeuverClassifications = maneuverClassificationsAggregator.improveManeuverClassifications(maneuverClassifications);
//        List<WindWithConfidence<Void>> windTrack = windTrackFromManeuverClassificationsExtractor.map
    }*/

}
