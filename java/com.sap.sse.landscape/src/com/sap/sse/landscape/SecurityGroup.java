package com.sap.sse.landscape;

import com.sap.sse.common.WithID;

@FunctionalInterface
public interface SecurityGroup extends WithID {
    @Override
    String getId();
}
