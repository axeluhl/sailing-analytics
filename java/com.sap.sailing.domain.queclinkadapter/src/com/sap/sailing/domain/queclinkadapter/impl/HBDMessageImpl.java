package com.sap.sailing.domain.queclinkadapter.impl;

import com.sap.sailing.domain.queclinkadapter.HBDMessage;
import com.sap.sailing.domain.queclinkadapter.MessageType;
import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;

public abstract class HBDMessageImpl extends MessageImpl implements HBDMessage {
    private final int protocolVersion;
    private final short countNumber;
    
    public HBDMessageImpl(Direction direction, int protocolVersion, short countNumber) {
        super(MessageType.HBD, direction);
        this.protocolVersion = protocolVersion;
        this.countNumber = countNumber;
    }

    @Override
    public int getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public short getCountNumber() {
        return countNumber;
    }
}
