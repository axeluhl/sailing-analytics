package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.WindFieldGenerator;

public class WindFieldGeneratorMeasured extends WindFieldGeneratorImpl implements WindFieldGenerator {
    
    public WindFieldGeneratorMeasured(Boundary boundary, WindControlParameters windParameters) {
        super(boundary, windParameters);
    }

    @Override
    public void generate(TimePoint start, TimePoint end, TimePoint step) {
        super.generate(start, end, step);
    }
    
    @Override
    public Wind getWind(TimedPosition timedPosition) {

        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(10.0, new DegreeBearingImpl(180.0));
        
        return new WindImpl(timedPosition.getPosition(), timedPosition.getTimePoint(),
                speedWithBearing);

    }
}
