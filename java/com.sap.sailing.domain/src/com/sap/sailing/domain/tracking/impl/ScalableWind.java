package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.AbstractPosition;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.scalablevalue.ScalableValue;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalablePosition;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableSpeedWithBearing;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Wind values are scaled by separately scaling their speed and bearing, and separately scaling their time point, and
 * separately scaling their position. For the separate speed/bearing scaling see also {@link ScalableSpeedWithBearing}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class ScalableWind implements ScalableValue<ScalableWind, Wind> {
    private final ScalablePosition scalablePosition;
    private final double scaledTimePointSumInMilliseconds;
    private final ScalableSpeedWithBearing scalableSpeedWithBearing;
    
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
    
    public boolean useSpeed() {
        return useSpeed;
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
        return new ScalableWind(scalablePosition == null ? t.getValue().scalablePosition : scalablePosition.add(t
                .getValue().scalablePosition), scaledTimePointSumInMilliseconds
                + t.getValue().scaledTimePointSumInMilliseconds, this.scalableSpeedWithBearing.add(t.getValue().scalableSpeedWithBearing),
                useSpeed || t.getValue().useSpeed);
    }

    @Override
    public ScalableWind getValue() {
        return this;
    }

    @Override
    public Wind divide(double divisor) {
        return new WindImpl(scalablePosition == null ? null : new LazyDividedScaledPosition(scalablePosition, divisor),
                new MillisecondsTimePoint(
                        (long) (scaledTimePointSumInMilliseconds / divisor)), scalableSpeedWithBearing.divide(divisor));
    }

    /**
     * A wind object's position is currently not very frequently used. However, during averaging, computing the average
     * position of a number of wind fixes costs quite a few CPU cycles. By making this calculation lazy / on-demand we may
     * save some of them.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private static class LazyDividedScaledPosition extends AbstractPosition {
        private static final long serialVersionUID = -4755705843467806809L;
        private final ScalablePosition scalablePosition;
        private final double divisor;
        private Position position;
        
        public LazyDividedScaledPosition(ScalablePosition scalablePosition, double divisor) {
            super();
            this.scalablePosition = scalablePosition;
            this.divisor = divisor;
            this.position = null;
        }

        @Override
        public synchronized double getLatRad() {
            if (position == null) {
                resolve();
            }
            return position.getLatRad();
        }

        @Override
        public synchronized double getLngRad() {
            if (position == null) {
                resolve();
            }
            return position.getLngRad();
        }
        
        private synchronized void resolve() {
            position = scalablePosition.divide(divisor);
        }
    }
}
