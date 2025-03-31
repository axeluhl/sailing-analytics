package com.sap.sse.landscape;

import com.sap.sse.common.WithID;

public interface SecurityGroup extends WithID {
    @Override
    String getId();
    
    String getVpcId();
}
