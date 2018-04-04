package com.sap.sailing.domain.base;

import java.net.URI;

import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public interface CompetitorChangeListener extends NationalityChangeListener {
    void colorChanged(Color oldColor, Color newColor);
    
    void nameChanged(String oldName, String newName);

    void shortNameChanged(String oldShortName, String newShortName);

    void emailChanged(String oldEmail, String newEmail);

    void searchTagChanged(String oldSearchTag, String newSearchTag);

    void flagImageChanged(URI oldFlagImageURI, URI newFlagImageURI);
    
    void timeOnTimeFactorChanged(Double oldTimeOnTimeFactor, Double newTimeOnTimeFactor);

    void timeOnDistanceAllowancePerNauticalMileChanged(Duration oldTimeOnDistanceAllowancePerNauticalMile, Duration newTimeOnDistanceAllowancePerNauticalMile);
}
