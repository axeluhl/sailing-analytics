package com.sap.sailing.domain.base;

import com.sap.sse.datamining.annotations.Connector;

public interface WithNationality {
    @Connector(messageKey="Nationality", ordinal=8)
    Nationality getNationality();
}
