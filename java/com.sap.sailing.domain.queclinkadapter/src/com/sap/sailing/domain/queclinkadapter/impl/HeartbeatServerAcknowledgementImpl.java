package com.sap.sailing.domain.queclinkadapter.impl;

import com.sap.sailing.domain.queclinkadapter.HeartbeatServerAcknowledgement;
import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;

public class HeartbeatServerAcknowledgementImpl extends HeartbeatMessageImpl implements HeartbeatServerAcknowledgement {
    public HeartbeatServerAcknowledgementImpl(int protocolVersion, short countNumber) {
        super(Direction.ACK, protocolVersion, countNumber);
    }

    @Override
    public String[] getParameters() {
        return new String[] { QueclinkStreamParserImpl.formatProtocolVersionHex(getProtocolVersion()), QueclinkStreamParserImpl.formatCountNumberHex(getCountNumber()) };
    }
}
