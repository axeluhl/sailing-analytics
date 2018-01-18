package com.sap.sailing.windestimation.impl.classifier;

public class PointOfSailAtLowestSpeedWithinManeuverMainCurveLikelihooks {

    private final double lowestSpeedPointRefersUpwind;
    private final double lowestSpeedPointRefersDownwind;
    private final double lowestSpeedPointRefersReaching;

    public PointOfSailAtLowestSpeedWithinManeuverMainCurveLikelihooks(double lowestSpeedPointRefersUpwind,
            double lowestSpeedPointRefersDownwind, double lowestSpeedPointRefersReaching) {
        this.lowestSpeedPointRefersUpwind = lowestSpeedPointRefersUpwind;
        this.lowestSpeedPointRefersDownwind = lowestSpeedPointRefersDownwind;
        this.lowestSpeedPointRefersReaching = lowestSpeedPointRefersReaching;
    }

    public double getLowestSpeedPointRefersUpwind() {
        return lowestSpeedPointRefersUpwind;
    }

    public double getLowestSpeedPointRefersDownwind() {
        return lowestSpeedPointRefersDownwind;
    }

    public double getLowestSpeedPointRefersReaching() {
        return lowestSpeedPointRefersReaching;
    }

}
