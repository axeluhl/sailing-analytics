package com.sap.sailing.domain.common;

import java.io.Serializable;

import com.sap.sse.datamining.shared.annotations.Dimension;

public interface Named extends Serializable {
    @Dimension(messageKey="Name")
    String getName();
}
