package com.sap.sailing.domain.queclinkadapter;

import com.sap.sailing.domain.queclinkadapter.impl.HBDAcknowledgementImpl;
import com.sap.sse.common.TimePoint;

public interface HBDAcknowledgement extends HBDMessage, Acknowledgement {
    MessageFactory FACTORY = HBDAcknowledgementImpl::createFromParameters;
    
    String getImei();
    
    String getDeviceName();
    
    TimePoint getSendTime();
}
