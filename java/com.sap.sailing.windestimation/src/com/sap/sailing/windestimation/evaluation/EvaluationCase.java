package com.sap.sailing.windestimation.evaluation;

import java.util.Map;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.windestimation.WindEstimationComponentWithInternals;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sse.common.TimePoint;

public class EvaluationCase<T> {

    private final WindEstimationComponentWithInternals<RaceWithEstimationData<T>> windEstimator;
    private final RaceWithEstimationData<ManeuverForEstimation> race;
    private final Map<TimePoint, Wind> targetWindFixesPerTimePoint;

    public EvaluationCase(WindEstimationComponentWithInternals<RaceWithEstimationData<T>> windEstimator,
            RaceWithEstimationData<ManeuverForEstimation> race, Map<TimePoint, Wind> targetWindFixesPerTimePoint) {
        this.windEstimator = windEstimator;
        this.race = race;
        this.targetWindFixesPerTimePoint = targetWindFixesPerTimePoint;
    }

    public WindEstimationComponentWithInternals<RaceWithEstimationData<T>> getWindEstimator() {
        return windEstimator;
    }

    public RaceWithEstimationData<ManeuverForEstimation> getRace() {
        return race;
    }

    public Map<TimePoint, Wind> getTargetWindFixesPerTimePoint() {
        return targetWindFixesPerTimePoint;
    }

}
