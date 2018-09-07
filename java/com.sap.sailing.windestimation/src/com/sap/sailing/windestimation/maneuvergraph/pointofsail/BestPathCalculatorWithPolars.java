package com.sap.sailing.windestimation.maneuvergraph.pointofsail;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;
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
                int absTwaInDegrees = previousPointOfSail.getTwa();
                if (absTwaInDegrees > 180) {
                    absTwaInDegrees = 360 - absTwaInDegrees;
                }
                WindSpeedRange currentTwaWindSpeedRange = polarsFittingWindEstimation.getWindSpeedRange(boatClass,
                        avgSpeedInKnots, absTwaInDegrees);
                if (currentTwaWindSpeedRange != null) {
                    windSpeedRange = windSpeedRange == null ? currentTwaWindSpeedRange
                            : windSpeedRange.extend(currentTwaWindSpeedRange);
                }
            }
        }
        int absTwaInDegrees = pointOfSail.getTwa();
        if (absTwaInDegrees > 180) {
            absTwaInDegrees = 360 - absTwaInDegrees;
        }
        WindSpeedRange newTwaWindSpeedRange = polarsFittingWindEstimation.getWindSpeedRange(boatClass,
                speedAtPointOfSail, absTwaInDegrees);
        if (newTwaWindSpeedRange == null) {
            return 0.8;
        }
        WindSpeedRange intersectedWindSpeedRange = windSpeedRange.intersect(newTwaWindSpeedRange);
        double penaltyFactor = intersectedWindSpeedRange.getConfidence()
                * (1 / Math.max(1, (48 + intersectedWindSpeedRange.getSpeedDifference()) / 50));
        return penaltyFactor;
    }

}
