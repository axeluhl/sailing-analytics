package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.windestimation.WindEstimationComponentWithInternals;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public class EvaluationCase<T> {

    private final WindEstimationComponentWithInternals<RaceWithEstimationData<T>> windEstimator;
    private final RaceWithEstimationData<ManeuverForEstimation> race;
    private final WindTrack targetWindTrack;

    public EvaluationCase(WindEstimationComponentWithInternals<RaceWithEstimationData<T>> windEstimator,
            RaceWithEstimationData<ManeuverForEstimation> race, WindTrack targetWindTrack) {
        this.windEstimator = windEstimator;
        this.race = race;
        this.targetWindTrack = targetWindTrack;
    }

    public WindEstimationComponentWithInternals<RaceWithEstimationData<T>> getWindEstimator() {
        return windEstimator;
    }

    public RaceWithEstimationData<ManeuverForEstimation> getRace() {
        return race;
    }

    public WindTrack getTargetWindTrack() {
        return targetWindTrack;
    }

}
