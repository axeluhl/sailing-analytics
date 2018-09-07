package com.sap.sailing.windestimation.maneuvergraph.maneuvernode;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;
import com.sap.sailing.windestimation.polarsfitting.WindSpeedRange;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class ManeuverNodeBestPathCalculatorWithPolars extends ManeuverNodeBestPathsCalculator {

    private final PolarsFittingWindEstimation polarsFittingWindEstimation;

    public ManeuverNodeBestPathCalculatorWithPolars(PolarsFittingWindEstimation polarsFittingWindEstimation) {
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

    @Override
    public Speed getAvgWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        WindSpeedRange windSpeedRange = null;
        BoatClass boatClass = maneuver.getBoatClass();
        if(maneuver.isCleanBefore()) {
            double absTwaInDegrees = Math.abs(windCourse.reverse().getDifferenceTo(maneuver.getAverageSpeedWithBearingBefore().getBearing()).getDegrees());
            double avgSpeedInKnots = maneuver.getAverageSpeedWithBearingBefore().getBearing().getDegrees();
            windSpeedRange = polarsFittingWindEstimation.getWindSpeedRange(boatClass,
                    avgSpeedInKnots, absTwaInDegrees);
        }
        if(maneuver.isCleanAfter()) {
            double absTwaInDegrees = Math.abs(windCourse.reverse().getDifferenceTo(maneuver.getAverageSpeedWithBearingAfter().getBearing()).getDegrees());
            double avgSpeedInKnots = maneuver.getAverageSpeedWithBearingAfter().getBearing().getDegrees();
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
