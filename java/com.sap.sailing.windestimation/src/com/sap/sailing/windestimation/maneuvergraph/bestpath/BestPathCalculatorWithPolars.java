package com.sap.sailing.windestimation.maneuvergraph.bestpath;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;
import com.sap.sailing.windestimation.polarsfitting.WindSpeedRange;

public class BestPathCalculatorWithPolars extends BestPathsCalculator {

    private final PolarsFittingWindEstimation polarsFittingWindEstimation;

    public BestPathCalculatorWithPolars(PolarsFittingWindEstimation polarsFittingWindEstimation) {
        this.polarsFittingWindEstimation = polarsFittingWindEstimation;
    }

    @Override
    protected double getSpeedPenaltyFactorForPointOfSail(SailingStatistics speedStatistics,
            FineGrainedPointOfSail pointOfSail, double speedAtPointOfSail, BoatClass boatClass) {
        WindSpeedRange windSpeedRange = null;
        for (FineGrainedPointOfSail previousPointOfSail : FineGrainedPointOfSail.values()) {
            if (speedStatistics.getNumberOfCleanTracks(previousPointOfSail) > 0) {
                double avgSpeedInKnots = speedStatistics.getAverageSpeedInKnotsForPointOfSail(previousPointOfSail);
                int absTwaInDegrees = pointOfSail.getTwa();
                WindSpeedRange currentTwaWindSpeedRange = polarsFittingWindEstimation.getWindSpeedRange(boatClass,
                        avgSpeedInKnots, absTwaInDegrees);
                if (currentTwaWindSpeedRange != null) {
                    windSpeedRange = windSpeedRange == null ? currentTwaWindSpeedRange
                            : windSpeedRange.extend(currentTwaWindSpeedRange);
                }
            }
        }
        WindSpeedRange newTwaWindSpeedRange = polarsFittingWindEstimation.getWindSpeedRange(boatClass,
                speedAtPointOfSail, Math.abs(pointOfSail.getTwa()));
        if (newTwaWindSpeedRange == null) {
            return 0.8;
        }
        WindSpeedRange intersectedWindSpeedRange = windSpeedRange.intersect(newTwaWindSpeedRange);
        double penaltyFactor = intersectedWindSpeedRange.getConfidence()
                * (1 / Math.max(1, (48 + intersectedWindSpeedRange.getSpeedDifference()) / 50));
        return penaltyFactor;
    }

}
