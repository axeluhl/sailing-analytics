package com.sap.sailing.domain.common.confidence.impl;

import com.sap.sailing.domain.common.DoublePair;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.scalablevalue.IsScalable;
import com.sap.sse.common.scalablevalue.ScalableValue;

public class BearingWithConfidenceImpl<RelativeTo> extends HasConfidenceImpl<DoublePair, Bearing, RelativeTo>
implements BearingWithConfidence<RelativeTo>, IsScalable<DoublePair, Bearing> {
    private static final long serialVersionUID = 1624026377840747818L;

    public BearingWithConfidenceImpl(Bearing bearing, double confidence, RelativeTo relativeTo) {
        super(bearing, confidence, relativeTo);
    }
    
    @Override
    public ScalableValue<DoublePair, Bearing> getScalableValue() {
        return new ScalableBearing(getObject());
    }
}
