package com.sap.sailing.simulator;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.Set;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;

public interface PolarDiagram extends Serializable {

    // TO BE REVIEWED
    // not sure whether I use the right terms
    enum WindSide {
        RIGHT, LEFT, FACING, OPPOSING
    };

    SpeedWithBearing getWind();

    void setWind(SpeedWithBearing newWind);

    void setCurrent(SpeedWithBearing newCurrent);

    SpeedWithBearing getCurrent();

    Bearing getTargetDirection();

    void setTargetDirection(Bearing newTargetDirection);

    SpeedWithBearing getSpeedAtBearing(Bearing bearing);

    SpeedWithBearing getSpeedAtBearingOverGround(Bearing bearing);

    SpeedWithBearing[] optimalVMGUpwind();

    Bearing[] optimalDirectionsUpwind();

    Bearing[] optimalDirectionsDownwind();

    long getTurnLoss();

    WindSide getWindSide(Bearing bearing);

    NavigableMap<Speed, NavigableMap<Bearing, Speed>> polarDiagramPlot(Double bearingStep, Set<Speed> extraSpeeds);

    NavigableMap<Speed, NavigableMap<Bearing, Speed>> polarDiagramPlot(Double bearingStep);

    NavigableMap<Speed, NavigableMap<Bearing, Speed>> getSpeedTable();

    NavigableMap<Speed, Bearing> getBeatAngles();

    NavigableMap<Speed, Bearing> getGybeAngles();

    NavigableMap<Speed, Speed> getBeatSOG();

    NavigableMap<Speed, Speed> getGybeSOG();

}
