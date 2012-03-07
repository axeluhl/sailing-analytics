package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.impl.HasConfidenceImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindWithConfidence;

public class WindWithConfidenceImpl<RelativeTo> extends HasConfidenceImpl<ScalableWind, Wind, RelativeTo> implements WindWithConfidence<RelativeTo> {

    public WindWithConfidenceImpl(Wind object, double confidence, RelativeTo relativeTo) {
        super(object, confidence, relativeTo);
    }

    @Override
    public ScalableWind getScalableValue() {
        return new ScalableWind(getObject());
    }

    @Override
    public String toString() {
        return (getObject() != null ? getObject().toString() : "null")+"@"+getConfidence();
    }
}
