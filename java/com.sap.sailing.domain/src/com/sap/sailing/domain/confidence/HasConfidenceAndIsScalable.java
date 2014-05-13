package com.sap.sailing.domain.confidence;

import com.sap.sailing.domain.common.scalablevalue.IsScalable;

public interface HasConfidenceAndIsScalable<ValueType, BaseType, RelativeTo> extends IsScalable<ValueType, BaseType>,
        HasConfidence<ValueType, BaseType, RelativeTo> {
}
