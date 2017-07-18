package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.PositionWithConfidence;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.confidence.impl.HasConfidenceImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalablePosition;
import com.sap.sse.common.scalablevalue.IsScalable;

public class PositionWithConfidenceImpl<RelativeTo> extends HasConfidenceImpl<ScalablePosition, Position, RelativeTo> implements
        PositionWithConfidence<RelativeTo>, IsScalable<ScalablePosition, Position> {
    private static final long serialVersionUID = -3857039006152257909L;

    public PositionWithConfidenceImpl(Position position, double confidence, RelativeTo relativeTo) {
        super(position, confidence, relativeTo);
    }

    @Override
    public ScalablePosition getScalableValue() {
        return new ScalablePosition(getObject());
    }
}
