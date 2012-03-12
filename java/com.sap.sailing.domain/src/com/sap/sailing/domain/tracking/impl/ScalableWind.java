package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.ScalablePosition;
import com.sap.sailing.domain.base.impl.ScalableSpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;
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
     * Speeds are combined separately from the other values. A {@link ScalableWind} may use its {@link #useSpeed} flag
     * to keep its speed value from being considered during addition with other values that do use their speed. For this
     * case, the confidence sum for the speed needs to be passed along so that during {@link #divide(double)} the speed
     * can be divided by a different divisor. Must be 0.0 in case {@link #useSpeed} is <code>false</code>.
     */
    private final double speedConfidenceSum;
    
    /**
     * if <code>false</code>, the {@link ScalableSpeedWithBearing}'s speed is ignored during an {@link #add(ScalableValue)} operation
     * with another {@link ScalableWind} object for which {@link #useSpeed} is <code>true</code>.
     */
    private final boolean useSpeed;
    
    /**
     * @param speedConfidenceSum must be <code>0.0</code> if <code>useSpeed==false</code>
     */
    public ScalableWind(Wind wind, boolean useSpeed, double speedConfidenceSum) {
        assert useSpeed || speedConfidenceSum==0.0;
        this.scalablePosition = wind.getPosition() == null ? null : new ScalablePosition(wind.getPosition());
        this.scaledTimePointSumInMilliseconds = wind.getTimePoint().asMillis();
        this.scalableSpeedWithBearing = new ScalableSpeedWithBearing(wind);
        this.useSpeed = useSpeed;
        this.speedConfidenceSum = speedConfidenceSum;
    }
    
    /**
     * @param speedConfidenceSum must be <code>0.0</code> if <code>useSpeed==false</code>
     */
    private ScalableWind(ScalablePosition scalablePosition, double scaledTimePointSumInMilliseconds,
            ScalableSpeedWithBearing scalableSpeedWithBearing, boolean useSpeed, double speedConfidenceSum) {
        super();
        assert useSpeed || speedConfidenceSum==0.0;
        this.scalablePosition = scalablePosition;
        this.scaledTimePointSumInMilliseconds = scaledTimePointSumInMilliseconds;
        this.scalableSpeedWithBearing = scalableSpeedWithBearing;
        this.useSpeed = useSpeed;
        this.speedConfidenceSum = speedConfidenceSum;
    }

    @Override
    public ScalableWind multiply(double factor) {
        return new ScalableWind(scalablePosition == null ? null : scalablePosition.multiply(factor),
                factor * scaledTimePointSumInMilliseconds, scalableSpeedWithBearing.multiply(factor), useSpeed, factor*speedConfidenceSum);
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
        double newSpeedConfidenceSum = useSpeed ? speedConfidenceSum : 0.0;
        if (this.useSpeed || (t instanceof ScalableWind && !((ScalableWind) t).useSpeed)) {
            thisSSWB = this.scalableSpeedWithBearing;
        } else {
            // use t's speed also as thisSSWB's speed:
            thisSSWB = new ScalableSpeedWithBearing(new KnotSpeedWithBearingImpl(/* wind "is a" speed */ t.getValue().divide(1.0).getKnots(),
                    this.scalableSpeedWithBearing.divide(1.0).getBearing()));
        }
        newSpeedConfidenceSum += (t instanceof ScalableWind && ((ScalableWind) t).useSpeed) ? ((ScalableWind) t).speedConfidenceSum : 0.0;
        if (t instanceof ScalableWind && ((ScalableWind) t).useSpeed || !this.useSpeed) {
            tSSWB = ((ScalableWind) t).scalableSpeedWithBearing;
        } else {
            // use this's speed also as tSSWB's speed:
            tSSWB = new ScalableSpeedWithBearing(new KnotSpeedWithBearingImpl(/* wind "is a" speed */ this.scalableSpeedWithBearing.divide(1.0).getKnots(),
                    t.getValue().scalableSpeedWithBearing.divide(1.0).getBearing()));
        }
        return new ScalableWind(scalablePosition == null ? t.getValue().scalablePosition : scalablePosition.add(t.getValue().scalablePosition),
                scaledTimePointSumInMilliseconds+t.getValue().scaledTimePointSumInMilliseconds,
                thisSSWB.add(tSSWB), useSpeed || (t instanceof ScalableWind) && ((ScalableWind) t).useSpeed, newSpeedConfidenceSum);
    }

    @Override
    public Wind divide(double divisor) {
        // TODO this is ugly; if we need to keep the confidence sum for speed in the scalable object, we should also keep the confidence sum for all other values in the scalable value
        Bearing resultBearing = scalableSpeedWithBearing.divide(divisor).getBearing();
        // avoid NPE in case we've only averaged wind fixes that have useSpeed==false
        double resultSpeedInKnots = scalableSpeedWithBearing.divide(1.0).getKnots() / (speedConfidenceSum==0.0?divisor:speedConfidenceSum);
        return new WindImpl(scalablePosition == null ? null : scalablePosition.divide(divisor), new MillisecondsTimePoint(
                (long) (scaledTimePointSumInMilliseconds / divisor)),
                new KnotSpeedWithBearingImpl(resultSpeedInKnots, resultBearing));
    }

    @Override
    public ScalableWind getValue() {
        return this;
    }

}
