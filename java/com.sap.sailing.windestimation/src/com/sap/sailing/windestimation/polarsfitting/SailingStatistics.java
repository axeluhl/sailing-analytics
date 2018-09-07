package com.sap.sailing.windestimation.polarsfitting;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.windestimation.data.FineGrainedManeuverType;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Speed;

public class SailingStatistics implements Cloneable {
    // maneuver data
    private double[] sumOfAbsCourseChangesInDegreesPerManeuverType = new double[FineGrainedManeuverType
            .values().length];
    private double[] sumOfTurningRatesPerManeuverType = new double[sumOfAbsCourseChangesInDegreesPerManeuverType.length];
    private double[] sumOfSpeedLossesPerManeuverType = new double[sumOfAbsCourseChangesInDegreesPerManeuverType.length];
    private int[] maneuversCountPerManeuverType = new int[sumOfAbsCourseChangesInDegreesPerManeuverType.length];
    // speed data
    private double[] sumOfAverageSpeedsPerPointOfSail = new double[FineGrainedPointOfSail.values().length];
    private int[] trackCountPerPointOfSail = new int[sumOfAverageSpeedsPerPointOfSail.length];
    private double lowestUpwindAvgSpeed = 0;

    public double getAverageAbsCourseChangeInDegreesForManeuverType(FineGrainedManeuverType maneuverType) {
        return maneuversCountPerManeuverType[maneuverType.ordinal()] == 0 ? 0
                : sumOfAbsCourseChangesInDegreesPerManeuverType[maneuverType.ordinal()]
                        / maneuversCountPerManeuverType[maneuverType.ordinal()];
    }

    public double getAverageTurningRateForManeuverType(FineGrainedManeuverType maneuverType) {
        return maneuversCountPerManeuverType[maneuverType.ordinal()] == 0 ? 0
                : sumOfTurningRatesPerManeuverType[maneuverType.ordinal()]
                        / maneuversCountPerManeuverType[maneuverType.ordinal()];
    }

    public double getAverageSpeedLossForManeuverType(FineGrainedManeuverType maneuverType) {
        return maneuversCountPerManeuverType[maneuverType.ordinal()] == 0 ? 0
                : sumOfSpeedLossesPerManeuverType[maneuverType.ordinal()]
                        / maneuversCountPerManeuverType[maneuverType.ordinal()];
    }

    public double getAverageSpeedInKnotsForPointOfSail(FineGrainedPointOfSail pointOfSail) {
        return trackCountPerPointOfSail[pointOfSail.ordinal()] == 0 ? 0
                : sumOfAverageSpeedsPerPointOfSail[pointOfSail.ordinal()]
                        / trackCountPerPointOfSail[pointOfSail.ordinal()];
    }

    public double getLowestUpwindAvgSpeed() {
        return lowestUpwindAvgSpeed;
    }

    public void addRecordToStatistics(ManeuverForEstimation maneuver, FineGrainedManeuverType maneuverType,
            FineGrainedPointOfSail pointOfSailAfterManeuver) {
        if (maneuver.isClean()) {
            addRecordToStatistics(maneuver, maneuverType);
        }
        if (maneuver.isCleanBefore()) {
            FineGrainedPointOfSail pointOfSailBeforeManeuver = pointOfSailAfterManeuver
                    .getNextPointOfSail(maneuver.getCourseChangeInDegrees() * -1);
            addRecordToStatistics(maneuver.getAverageSpeedWithBearingBefore(), pointOfSailBeforeManeuver);
        }
        if (maneuver.isCleanAfter()) {
            addRecordToStatistics(maneuver.getAverageSpeedWithBearingAfter(), pointOfSailAfterManeuver);
        }
    }

    public void addRecordToStatistics(Speed boatSpeed, FineGrainedPointOfSail pointOfSail) {
        sumOfAverageSpeedsPerPointOfSail[pointOfSail.ordinal()] += boatSpeed.getKnots();
        trackCountPerPointOfSail[pointOfSail.ordinal()]++;
        double averageSpeedInKnotsForPointOfSail = getAverageSpeedInKnotsForPointOfSail(pointOfSail);
        if (pointOfSail.getLegType() == LegType.UPWIND
                && (lowestUpwindAvgSpeed == 0 || averageSpeedInKnotsForPointOfSail < lowestUpwindAvgSpeed)) {
            lowestUpwindAvgSpeed = averageSpeedInKnotsForPointOfSail;
        }
    }

    public void addRecordToStatistics(ManeuverForEstimation maneuver, FineGrainedManeuverType maneuverType) {
        sumOfAbsCourseChangesInDegreesPerManeuverType[maneuverType.ordinal()] += Math
                .abs(maneuver.getCourseChangeWithinMainCurveInDegrees());
        sumOfTurningRatesPerManeuverType[maneuverType.ordinal()] += maneuver.getMaxTurningRateInDegreesPerSecond();
        sumOfSpeedLossesPerManeuverType[maneuverType
                .ordinal()] += (maneuver.getSpeedLossRatio() + maneuver.getLowestSpeedVsExitingSpeedRatio()) / 2.0;
        maneuversCountPerManeuverType[maneuverType.ordinal()]++;
    }

    public int getNumberOfCleanManeuvers(FineGrainedManeuverType maneuverType) {
        return maneuversCountPerManeuverType[maneuverType.ordinal()];
    }

    public int getNumberOfCleanTracks(FineGrainedPointOfSail pointOfSail) {
        return trackCountPerPointOfSail[pointOfSail.ordinal()];
    }

    @Override
    public SailingStatistics clone() {
        SailingStatistics clone = new SailingStatistics();
        clone.lowestUpwindAvgSpeed = lowestUpwindAvgSpeed;
        clone.maneuversCountPerManeuverType = maneuversCountPerManeuverType.clone();
        clone.sumOfAbsCourseChangesInDegreesPerManeuverType = sumOfAbsCourseChangesInDegreesPerManeuverType.clone();
        clone.sumOfAverageSpeedsPerPointOfSail = sumOfAverageSpeedsPerPointOfSail.clone();
        clone.sumOfSpeedLossesPerManeuverType = sumOfSpeedLossesPerManeuverType.clone();
        clone.sumOfTurningRatesPerManeuverType = sumOfTurningRatesPerManeuverType.clone();
        clone.trackCountPerPointOfSail = trackCountPerPointOfSail.clone();
        return clone;
    }

    public SailingStatistics cloneAndAddRecordToStatistics(ManeuverForEstimation maneuver,
            FineGrainedManeuverType maneuverType, FineGrainedPointOfSail pointOfSailAfterManeuver) {
        SailingStatistics clone = this.clone();
        clone.addRecordToStatistics(maneuver, maneuverType, pointOfSailAfterManeuver);
        return clone;
    }

}