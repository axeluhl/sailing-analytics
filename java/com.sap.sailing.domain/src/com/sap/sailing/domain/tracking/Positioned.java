package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.Position;

public interface Positioned extends Serializable {
    Position getPosition();
}
