package com.sap.sailing.domain.base;

import com.sap.sse.datamining.shared.annotations.Connector;

public interface WithNationality {
    @Connector(ordinal=7)
    Nationality getNationality();
}
