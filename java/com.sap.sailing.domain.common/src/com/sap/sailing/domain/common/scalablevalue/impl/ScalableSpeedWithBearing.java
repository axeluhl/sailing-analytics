package com.sap.sailing.domain.common.scalablevalue.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sse.common.Util;
import com.sap.sse.common.scalablevalue.ScalableValue;

/**
 * Separately scales speed and bearing. Instead of considering speed and bearing a single vector that can be scaled, the
 * bearing is scaled separately, and the speed is scaled as a scalar value independently of the bearing. This is
 * particularly useful for {@link Wind} scaling where it makes more sense to average the wind speed independently of the
 * wind direction / bearing than adding up the "wind vectors" and averaging, which would reduce the resulting wind speed
 * for constant wind speeds across all fixes with different directions.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class ScalableSpeedWithBearing implements ScalableValue<Util.Triple<Speed, Double, Double>, SpeedWithBearing> {
    private final Speed speed;
    private final double sin;
    private final double cos;
    
    public ScalableSpeedWithBearing(SpeedWithBearing speedWithBearing) {
        this(new KnotSpeedImpl(speedWithBearing.getKnots()), Math.sin(speedWithBearing.getBearing()
                .getRadians()), Math.cos(speedWithBearing.getBearing().getRadians()));
    }
    
    public ScalableSpeedWithBearing(Speed speed, double sin, double cos) {
        this.speed = speed;
        this.sin = sin;
        this.cos = cos;
    }

    @Override
    public ScalableSpeedWithBearing multiply(double factor) {
        Speed newSpeed = new KnotSpeedImpl(factor*speed.getKnots());
        return new ScalableSpeedWithBearing(newSpeed, factor*sin, factor*cos);
    }

    @Override
    public ScalableSpeedWithBearing add(ScalableValue<Util.Triple<Speed, Double, Double>, SpeedWithBearing> t) {
        Speed newSpeed = new KnotSpeedImpl(speed.getKnots() + t.getValue().getA().getKnots());
        return new ScalableSpeedWithBearing(newSpeed, sin+t.getValue().getB(), cos+t.getValue().getC());
    }

    @Override
    public SpeedWithBearing divide(double divisor) {
        Speed newSpeed = new KnotSpeedImpl(speed.getKnots() / divisor);
        double angle;
        if (cos == 0) {
            angle = sin >= 0 ? Math.PI / 2 : -Math.PI / 2;
        } else {
            angle = Math.atan2(sin, cos);
        }
        Bearing bearing = new RadianBearingImpl(angle < 0 ? angle + 2 * Math.PI : angle);
        return new KnotSpeedWithBearingImpl(newSpeed.getKnots(), bearing);
    }

    @Override
    public Util.Triple<Speed, Double, Double> getValue() {
        return new Util.Triple<Speed, Double, Double>(speed, sin, cos);
    }
    
}