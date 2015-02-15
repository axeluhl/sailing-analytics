package com.sap.sailing.domain.base;

import com.sap.sse.common.Color;

public interface CompetitorChangeListener extends BoatChangeListener, NationalityChangeListener {
    void colorChanged(Color oldColor, Color newColor);
    
    void nameChanged(String oldName, String newName);
    
    void emailChanged(String oldEmail, String newEmail);
}
