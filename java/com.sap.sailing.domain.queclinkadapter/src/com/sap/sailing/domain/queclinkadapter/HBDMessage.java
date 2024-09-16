package com.sap.sailing.domain.queclinkadapter;

public interface HBDMessage extends Message {
    int getProtocolVersion();
    
    short getCountNumber();
}
