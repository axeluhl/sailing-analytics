package com.sap.sailing.domain.base;

import com.sap.sse.common.Color;

public interface BoatChangeListener {
    void nameChanged(String oldName, String newName);

    void colorChanged(Color oldColor, Color newColor);

    void sailIdChanged(String oldSailId, String newSailId);
}
