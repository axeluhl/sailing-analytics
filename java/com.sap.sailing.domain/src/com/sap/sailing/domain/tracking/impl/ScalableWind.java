package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.ScalablePosition;
import com.sap.sailing.domain.base.impl.ScalableSpeedWithBearing;
import com.sap.sailing.domain.confidence.ScalableValue;
import com.sap.sailing.domain.tracking.Wind;

public class ScalableWind implements ScalableValue<ScalableWind, Wind> {
    private final ScalablePosition scalablePosition;
    private final double scaledTimePointSumInMilliseconds;
    private final ScalableSpeedWithBearing scalableSpeedWithBearing;
    
    public ScalableWind(Wind wind) {
        this.scalablePosition = new ScalablePosition(wind.getPosition());
        this.scaledTimePointSumInMilliseconds = wind.getTimePoint().asMillis();
        this.scalableSpeedWithBearing = new ScalableSpeedWithBearing(wind);
    }
    
    private ScalableWind(ScalablePosition scalablePosition, double scaledTimePointSumInMilliseconds,
            ScalableSpeedWithBearing scalableSpeedWithBearing) {
        super();
        this.scalablePosition = scalablePosition;
        this.scaledTimePointSumInMilliseconds = scaledTimePointSumInMilliseconds;
        this.scalableSpeedWithBearing = scalableSpeedWithBearing;
    }

    @Override
    public ScalableWind multiply(double factor) {
        return new ScalableWind(scalablePosition.multiply(factor), factor*scaledTimePointSumInMilliseconds, scalableSpeedWithBearing.multiply(factor));
    }

    @Override
    public ScalableWind add(ScalableValue<ScalableWind, Wind> t) {
        return new ScalableWind(scalablePosition.add(t.getValue().scalablePosition),
                scaledTimePointSumInMilliseconds+t.getValue().scaledTimePointSumInMilliseconds,
                scalableSpeedWithBearing.add(t.getValue().scalableSpeedWithBearing));
    }

    @Override
    public Wind divide(double divisor) {
        return new WindImpl(scalablePosition.divide(divisor), new MillisecondsTimePoint(
                (long) (scaledTimePointSumInMilliseconds / divisor)), scalableSpeedWithBearing.divide(divisor));
    }

    @Override
    public ScalableWind getValue() {
        return this;
    }

}
