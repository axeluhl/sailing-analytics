package com.sap.sailing.domain.base;

import java.io.Serializable;

import com.sap.sse.common.Color;

public interface BoatChangeListener extends Serializable {
    void nameChanged(String oldName, String newName);

    void colorChanged(Color oldColor, Color newColor);

    void sailIdChanged(String oldSailId, String newSailId);
}
