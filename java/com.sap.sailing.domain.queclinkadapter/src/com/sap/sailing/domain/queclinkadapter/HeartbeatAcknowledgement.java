package com.sap.sailing.domain.queclinkadapter;

import com.sap.sse.common.TimePoint;

public interface HeartbeatAcknowledgement extends Acknowledgement {
    int getProtocolVersion();
    
    String getImei();
    
    String getDeviceName();
    
    TimePoint getSendTime();
    
    short getCountNumber();
}
