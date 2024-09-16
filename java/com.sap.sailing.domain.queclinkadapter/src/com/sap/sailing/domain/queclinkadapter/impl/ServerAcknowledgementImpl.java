package com.sap.sailing.domain.queclinkadapter.impl;

import com.sap.sailing.domain.queclinkadapter.ServerAcknowledgement;
import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;

public class ServerAcknowledgementImpl extends MessageImpl implements ServerAcknowledgement {
    private final short countNumber;
    
    public ServerAcknowledgementImpl(short countNumber) {
        super(/* type */ null, Direction.SACK);
        this.countNumber = countNumber;
    }
    
    @Override
    public String getPrefix() {
        return String.format("+%s:", getDirection());
    }

    @Override
    public String[] getParameters() {
        return new String[] { String.format("%4h", countNumber) };
    }
        
    public short getCountNumber() {
        return countNumber;
    }
}
