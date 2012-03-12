package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.ScalablePosition;
import com.sap.sailing.domain.base.impl.ScalableSpeedWithBearing;
import com.sap.sailing.domain.confidence.ScalableValue;
import com.sap.sailing.domain.tracking.Wind;

/**
 * Wind values are scaled by separately scaling their speed and bearing, and separately scaling their time point, and separately
 * scaling their position. For the separate speed/bearing scaling see also {@link ScalableSpeedWithBearing}.
 *  
 * @author Axel Uhl (d043530)
 *
 */
public class ScalableWind implements ScalableValue<ScalableWind, Wind> {
    private final ScalablePosition scalablePosition;
    private final double scaledTimePointSumInMilliseconds;
    private final ScalableSpeedWithBearing scalableSpeedWithBearing;
    
    /**
     * if <code>false</code>, the {@link ScalableSpeedWithBearing}'s speed is ignored during an {@link #add(ScalableValue)} operation
     * with another {@link ScalableWind} object for which {@link #useSpeed} is <code>true</code>.
     */
    private final boolean useSpeed;
    
    public ScalableWind(Wind wind, boolean useSpeed) {
        this.scalablePosition = wind.getPosition() == null ? null : new ScalablePosition(wind.getPosition());
        this.scaledTimePointSumInMilliseconds = wind.getTimePoint().asMillis();
        this.scalableSpeedWithBearing = new ScalableSpeedWithBearing(wind);
        this.useSpeed = useSpeed;
    }
    
    private ScalableWind(ScalablePosition scalablePosition, double scaledTimePointSumInMilliseconds,
            ScalableSpeedWithBearing scalableSpeedWithBearing, boolean useSpeed) {
        super();
        this.scalablePosition = scalablePosition;
        this.scaledTimePointSumInMilliseconds = scaledTimePointSumInMilliseconds;
        this.scalableSpeedWithBearing = scalableSpeedWithBearing;
        this.useSpeed = useSpeed;
    }

    @Override
    public ScalableWind multiply(double factor) {
        return new ScalableWind(scalablePosition == null ? null : scalablePosition.multiply(factor),
                factor * scaledTimePointSumInMilliseconds, scalableSpeedWithBearing.multiply(factor), useSpeed);
    }

    /**
     * If only one of <code>this</code> and <code>t</code> has <code>true</code> for {@link #useSpeed}, then its speed
     * is assumed for the other object's speed, effectively ignoring the other wind fix's speed as desired by setting
     * {@link #useSpeed} to <code>false</code>. The resulting object's {@link #useSpeed} is <code>true</code>, if at
     * least one of <code>this<code> and <code>t</code> has a <code>true</code> value for {@link #useSpeed}.
     */
    @Override
    public ScalableWind add(ScalableValue<ScalableWind, Wind> t) {
        ScalableSpeedWithBearing thisSSWB;
        ScalableSpeedWithBearing tSSWB;
        if (this.useSpeed || (t instanceof ScalableWind && !((ScalableWind) t).useSpeed)) {
            thisSSWB = this.scalableSpeedWithBearing;
        } else {
            // use t's speed also as thisSSWB's speed:
            thisSSWB = new ScalableSpeedWithBearing(new KnotSpeedWithBearingImpl(/* wind "is a" speed */ t.getValue().divide(1.0).getKnots(),
                    this.scalableSpeedWithBearing.divide(1.0).getBearing()));
        }
        if (t instanceof ScalableWind && ((ScalableWind) t).useSpeed || !this.useSpeed) {
            tSSWB = ((ScalableWind) t).scalableSpeedWithBearing;
        } else {
            // use this's speed also as tSSWB's speed:
            tSSWB = new ScalableSpeedWithBearing(new KnotSpeedWithBearingImpl(/* wind "is a" speed */ this.scalableSpeedWithBearing.divide(1.0).getKnots(),
                    t.getValue().scalableSpeedWithBearing.divide(1.0).getBearing()));
        }
        return new ScalableWind(scalablePosition == null ? t.getValue().scalablePosition : scalablePosition.add(t.getValue().scalablePosition),
                scaledTimePointSumInMilliseconds+t.getValue().scaledTimePointSumInMilliseconds,
                thisSSWB.add(tSSWB), useSpeed || (t instanceof ScalableWind) && ((ScalableWind) t).useSpeed);
    }

    @Override
    public Wind divide(double divisor) {
        return new WindImpl(scalablePosition == null ? null : scalablePosition.divide(divisor), new MillisecondsTimePoint(
                (long) (scaledTimePointSumInMilliseconds / divisor)), scalableSpeedWithBearing.divide(divisor));
    }

    @Override
    public ScalableWind getValue() {
        return this;
    }

}
