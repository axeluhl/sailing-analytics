package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.scalablevalue.IsScalable;
import com.sap.sailing.domain.scalablevalue.ScalableValue;
import com.sap.sailing.domain.scalablevalue.impl.ScalableSpeedWithBearing;

public class SpeedWithBearingWithConfidenceImpl<RelativeTo> extends
        HasConfidenceImpl<Triple<Speed, Double, Double>, SpeedWithBearing, RelativeTo> implements
        SpeedWithBearingWithConfidence<RelativeTo>, IsScalable<Triple<Speed, Double, Double>, SpeedWithBearing> {
    private static final long serialVersionUID = -4811576094614673625L;

    public SpeedWithBearingWithConfidenceImpl(SpeedWithBearing speedWithBearing, double confidence, RelativeTo relativeTo) {
        super(speedWithBearing, confidence, relativeTo);
    }

    /**
     * The scalable value used for averaging confidence-based objects of this type is a triple whose first component
     * holds the speed with a confidence while the second and third element are the sine and cosine values of the bearing's
     * angle.
     */
    @Override
    public ScalableValue<Triple<Speed, Double, Double>, SpeedWithBearing> getScalableValue() {
        return new ScalableSpeedWithBearing(getObject());
    }
}
