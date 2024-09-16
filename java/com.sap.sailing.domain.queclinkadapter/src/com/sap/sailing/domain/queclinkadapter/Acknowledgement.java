package com.sap.sailing.domain.queclinkadapter;

import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;

public interface Acknowledgement extends NonCommand {
    default Direction getDirection() {
        return Direction.ACK;
    }
}
