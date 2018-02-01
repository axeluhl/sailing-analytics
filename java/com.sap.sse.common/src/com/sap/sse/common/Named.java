package com.sap.sse.common;

import java.io.Serializable;

import com.sap.sse.datamining.annotations.Dimension;

public interface Named extends Serializable {
    @Dimension(messageKey="Name")
    String getName();
}
