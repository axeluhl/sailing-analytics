package com.sap.sailing.domain.common.scalablevalue.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.DoublePair;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.scalablevalue.ScalableValue;
import com.sap.sailing.domain.common.scalablevalue.ScalableValueWithDistance;

public class ScalableBearing implements ScalableValueWithDistance<DoublePair, Bearing> {
    private final double sin;
    private final double cos;
    
    public ScalableBearing(Bearing bearing) {
        this.sin = Math.sin(bearing.getRadians());
        this.cos = Math.cos(bearing.getRadians());
    }
    
    private ScalableBearing(double sin, double cos) {
        this.sin = sin;
        this.cos = cos;
    }
    
    @Override
    public ScalableBearing multiply(double factor) {
        DoublePair pair = getValue();
        return new ScalableBearing(factor*pair.getA(), factor*pair.getB());
    }

    @Override
    public ScalableBearing add(ScalableValue<DoublePair, Bearing> t) {
        DoublePair value = getValue();
        DoublePair tValue = t.getValue();
        return new ScalableBearing(value.getA()+tValue.getA(), value.getB()+tValue.getB());
    }

    /**
     * If the combined confidence was 0.0, no {@link Bearing} object can reasonably be computed; hence, <code>null</code>
     * is returned in such cases.
     */
    @Override
    public Bearing divide(double divisor) {
        Bearing result;
        if (sin == 0 && cos == 0) {
            result = null;
        } else {
            double angle;
            if (cos == 0) {
                angle = sin >= 0 ? Math.PI / 2 : -Math.PI / 2;
            } else {
                angle = Math.atan2(sin, cos);
            }
            result = new RadianBearingImpl(angle < 0 ? angle + 2 * Math.PI : angle);
        }
        return result;
    }

    @Override
    public DoublePair getValue() {
        return new DoublePair(sin, cos);
    }

    @Override
    public double getDistance(Bearing other) {
        return Math.abs(divide(1).getDifferenceTo(other).getDegrees());
    }
}
