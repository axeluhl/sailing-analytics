package com.sap.sailing.domain.queclinkadapter.impl;

import com.sap.sailing.domain.queclinkadapter.MessageType;
import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;

public abstract class HeartbeatMessageImpl extends MessageImpl {
    private final int protocolVersion;
    private final short countNumber;
    
    public HeartbeatMessageImpl(Direction direction, int protocolVersion, short countNumber) {
        super(MessageType.HBD, direction);
        this.protocolVersion = protocolVersion;
        this.countNumber = countNumber;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public short getCountNumber() {
        return countNumber;
    }
}
