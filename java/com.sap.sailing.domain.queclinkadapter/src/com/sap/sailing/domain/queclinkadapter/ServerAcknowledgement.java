package com.sap.sailing.domain.queclinkadapter;

import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;

public interface ServerAcknowledgement extends NonCommand {
    default Direction getDirection() {
        return Direction.SACK;
    }
}
