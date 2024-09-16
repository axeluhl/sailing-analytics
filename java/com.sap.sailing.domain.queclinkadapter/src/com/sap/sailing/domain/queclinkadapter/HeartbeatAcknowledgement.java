package com.sap.sailing.domain.queclinkadapter;

import com.sap.sailing.domain.queclinkadapter.impl.HeartbeatAcknowledgementImpl;
import com.sap.sse.common.TimePoint;

public interface HeartbeatAcknowledgement extends Acknowledgement {
    MessageFactory FACTORY = HeartbeatAcknowledgementImpl::createFromParameters;
    
    int getProtocolVersion();
    
    String getImei();
    
    String getDeviceName();
    
    TimePoint getSendTime();
    
    short getCountNumber();
}
