package com.sap.sailing.domain.base;

import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface WithNationality {
    @SideEffectFreeValue(messageKey="Nationality")
    Nationality getNationality();
}
