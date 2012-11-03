package com.sap.sailing.simulator.windfield.impl;

import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

public class WindFieldGeneratorMeasured extends WindFieldGeneratorImpl implements WindFieldGenerator {
    
    private static final long serialVersionUID = -7436152672809530764L;
    
    protected Path gpsWind;

//    public WindFieldGeneratorMeasured() {
//        super();
//    }
    
    public WindFieldGeneratorMeasured(Boundary boundary, WindControlParameters windParameters) {
        super(boundary, windParameters);
    }

    public void setGPSWind(Path gpsWind) {
        this.gpsWind = gpsWind;
    }

    @Override
    public void generate(TimePoint start, TimePoint end, TimePoint step) {
        super.generate(start, end, step);
    }

    @Override
    public Wind getWind(TimedPosition timedPosition) {

        TimePoint timePoint = timedPosition.getTimePoint();
        TimedPositionWithSpeed p1 = null;
        TimedPositionWithSpeed p2 = null;
        List<TimedPositionWithSpeed> pathPoints = gpsWind.getPathPoints();
        for(TimedPositionWithSpeed p : pathPoints) {
            p2 = p;
            if (p.getTimePoint().after(timePoint)) {
                
                break;
            }
            p1 = p;
        }
        
        // TODO: interpolate between p1 and p2
        // TODO: check for race with true wind measurement; current test race has everyone 1kn wind speed
        //System.out.println("bear p1: "+p1.getSpeed().getBearing().getDegrees()+"  p2: "+p2.getSpeed().getBearing().getDegrees());
        Bearing midBear = new DegreeBearingImpl((p1.getSpeed().getBearing().getDegrees()+p2.getSpeed().getBearing().getDegrees())/2.);
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl((p1.getSpeed().getKnots()+p2.getSpeed().getKnots())/2., midBear);
        
        return new WindImpl(timedPosition.getPosition(), timedPosition.getTimePoint(), speedWithBearing);

    }
}
