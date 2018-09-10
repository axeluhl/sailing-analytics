package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;
import com.sap.sailing.windestimation.polarsfitting.WindSpeedRange;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class BestPathCalculatorWithPolars extends BestPathsCalculator {

    private final PolarsFittingWindEstimation polarsFittingWindEstimation;
    private final boolean speedPenalty;

    public BestPathCalculatorWithPolars(PolarsFittingWindEstimation polarsFittingWindEstimation, boolean speedPenalty) {
        this.polarsFittingWindEstimation = polarsFittingWindEstimation;
        this.speedPenalty = speedPenalty;
    }

    @Override
    protected double getSpeedPenaltyFactorForPointOfSail(SailingStatistics speedStatistics,
            FineGrainedPointOfSail pointOfSail, double speedAtPointOfSail, BoatClass boatClass) {
        double penaltyFactor = 1;
        if(speedPenalty) {
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
            if (newTwaWindSpeedRange == null || windSpeedRange == null) {
                return 0.8;
            }
            WindSpeedRange intersectedWindSpeedRange = windSpeedRange == null ? null : windSpeedRange.intersect(newTwaWindSpeedRange);
            penaltyFactor = intersectedWindSpeedRange.getConfidence()
                    * (1 / Math.max(1, (9 + intersectedWindSpeedRange.getSpeedDifference()) / 10));
        }
        return penaltyFactor;
    }

    @Override
    public Speed getWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        WindSpeedRange windSpeedRange = null;
        BoatClass boatClass = maneuver.getBoatClass();
        if(maneuver.isCleanBefore()) {
            double absTwaInDegrees = Math.abs(windCourse.reverse().getDifferenceTo(maneuver.getAverageSpeedWithBearingBefore().getBearing()).getDegrees());
            double avgSpeedInKnots = maneuver.getAverageSpeedWithBearingBefore().getKnots();
            windSpeedRange = polarsFittingWindEstimation.getWindSpeedRange(boatClass,
                    avgSpeedInKnots, absTwaInDegrees);
        }
        if(maneuver.isCleanAfter()) {
            double absTwaInDegrees = Math.abs(windCourse.reverse().getDifferenceTo(maneuver.getAverageSpeedWithBearingAfter().getBearing()).getDegrees());
            double avgSpeedInKnots = maneuver.getAverageSpeedWithBearingAfter().getKnots();
            WindSpeedRange currentTwaWindSpeedRange = polarsFittingWindEstimation.getWindSpeedRange(boatClass,
                    avgSpeedInKnots, absTwaInDegrees);
            if (currentTwaWindSpeedRange != null) {
                windSpeedRange = windSpeedRange == null ? currentTwaWindSpeedRange
                        : windSpeedRange.extend(currentTwaWindSpeedRange);
            }
        }
        if(windSpeedRange != null) {
            return new KnotSpeedImpl(windSpeedRange.getMiddleSpeed());
        }
        return new KnotSpeedImpl(0.0);
    }

}
