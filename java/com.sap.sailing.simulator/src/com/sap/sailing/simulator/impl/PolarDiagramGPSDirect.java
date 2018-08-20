package com.sap.sailing.simulator.impl;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.simulator.BoatDirection;
import com.sap.sailing.simulator.PointOfSail;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.Util.Pair;

public class PolarDiagramGPSDirect implements PolarDiagram, Serializable {

    private static final long serialVersionUID = -9219705955440602679L;
    private final BoatClass boatClass;
    private final PolarDataService polarData;
    private SpeedWithBearing wind;
    private SpeedWithBearing beatPort = null;
    private SpeedWithBearing beatStar = null;
    private SpeedWithBearing jibePort = null;
    private SpeedWithBearing jibeStar = null;

    public PolarDiagramGPSDirect(BoatClass boatClass, PolarDataService polarData) {
        this.boatClass = boatClass;
        this.polarData = polarData;
    }

    @Override
    public void setSpeedScale(double scaleSpeed) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getSpeedScale() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setBearingScale(double scaleBearing) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getBearingScale() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SpeedWithBearing getWind() {
        return this.wind;
    }

    @Override
    public void setWind(SpeedWithBearing newWind) {
        if ((this.wind == null) || (!this.wind.equals(newWind))) {
            this.wind = newWind;
            this.beatPort = null;
            this.beatStar = null;
            this.jibePort = null;
            this.jibeStar = null;
        }
    }

    @Override
    public void initializeSOGwithCurrent() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setCurrent(SpeedWithBearing newCurrent) {
        // TODO Auto-generated method stub
    }

    @Override
    public SpeedWithBearing getCurrent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasCurrent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Bearing getTargetDirection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTargetDirection(Bearing newTargetDirection) {
        // TODO Auto-generated method stub

    }

    @Override
    public SpeedWithBearing getSpeedAtBearing(Bearing bearing) {
        if ((beatPort != null) && (bearing.equals(beatPort.getBearing()))) {
            return beatPort;
        } else if ((beatStar != null) && (bearing.equals(beatStar.getBearing()))) {
            return beatStar;
        } else if ((jibePort != null) && (bearing.equals(jibePort.getBearing()))) {
            return jibePort;
        } else if ((jibeStar != null) && (bearing.equals(jibeStar.getBearing()))) {
            return jibeStar;
        }
        Speed speed = null;
        try {
            speed = this.polarData.getSpeed(this.boatClass, this.wind, bearing.getDifferenceTo(wind.getBearing()))
                    .getObject();
        } catch (NotEnoughDataHasBeenAddedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new KnotSpeedWithBearingImpl(speed.getKnots(), bearing);
    }

    @Override
    public SpeedWithBearing getSpeedAtBearingOverGround(Bearing bearing) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpeedWithBearing[] optimalVMGUpwind() {
        return null;
    }

    @Override
    public Bearing[] optimalDirectionsUpwind() {
        try {
            if (beatPort == null) {
                SpeedWithBearing beatPortRelative = this.polarData.getAverageSpeedWithBearing(this.boatClass, wind,
                        LegType.UPWIND, Tack.PORT).getObject();
                beatPort = new KnotSpeedWithBearingImpl(beatPortRelative.getKnots(), this.wind.getBearing().reverse()
                        .add(beatPortRelative.getBearing()));
            }
            if (beatStar == null) {
                SpeedWithBearing beatStarRelative = this.polarData.getAverageSpeedWithBearing(this.boatClass, wind,
                        LegType.UPWIND, Tack.STARBOARD).getObject();
                beatStar = new KnotSpeedWithBearingImpl(beatStarRelative.getKnots(), this.wind.getBearing().reverse()
                        .add(beatStarRelative.getBearing()));
            }
        } catch (NotEnoughDataHasBeenAddedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Bearing[] result = new Bearing[2];
        if (beatPort != null) {
            result[0] = this.beatPort.getBearing();
        }
        if (beatStar != null) {
            result[1] = this.beatStar.getBearing();
        }
        return result;
    }

    @Override
    public Bearing[] optimalDirectionsDownwind() {
        try {
            if (jibePort == null) {
                SpeedWithBearing jibePortRelative = this.polarData.getAverageSpeedWithBearing(this.boatClass, wind,
                        LegType.DOWNWIND, Tack.PORT).getObject();
                jibePort = new KnotSpeedWithBearingImpl(jibePortRelative.getKnots(), this.wind.getBearing().reverse()
                        .add(jibePortRelative.getBearing()));
            }
            if (jibeStar == null) {
                SpeedWithBearing jibeStarRelative = this.polarData.getAverageSpeedWithBearing(this.boatClass, wind,
                        LegType.DOWNWIND, Tack.STARBOARD).getObject();
                jibeStar = new KnotSpeedWithBearingImpl(jibeStarRelative.getKnots(), this.wind.getBearing().reverse()
                        .add(jibeStarRelative.getBearing()));
            }
        } catch (NotEnoughDataHasBeenAddedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Bearing[] result = new Bearing[2];
        if (jibePort != null) {
            result[0] = this.jibePort.getBearing();
        }
        if (jibeStar != null) {
            result[1] = this.jibeStar.getBearing();
        }
        return result;
    }

    @Override
    public long getTurnLoss() {
        // TODO Auto-generated method stub
        return 4000;
    }

    @Override
    public WindSide getWindSide(Bearing bearing) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableMap<Speed, NavigableMap<Bearing, Speed>> polarDiagramPlot(Double bearingStep, Set<Speed> extraSpeeds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableMap<Speed, NavigableMap<Bearing, Speed>> polarDiagramPlot(Double bearingStep) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableMap<Speed, NavigableMap<Bearing, Speed>> getSpeedTable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableMap<Speed, Bearing> getBeatAngles() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableMap<Speed, Bearing> getJibeAngles() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableMap<Speed, Speed> getBeatSOG() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableMap<Speed, Speed> getJibeSOG() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Pair<PointOfSail, BoatDirection> getPointOfSail(Bearing bearTarget) {
        // TODO Auto-generated method stub
        return null;
    }

}
