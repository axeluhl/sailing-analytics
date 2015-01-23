package com.sap.sailing.domain.base.impl;

import java.net.URI;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.NationalityChangeListener;
import com.sap.sailing.domain.base.Team;

public interface DynamicTeam extends Team {
    void setNationality(Nationality newNationality);

    void addNationalityChangeListener(NationalityChangeListener listener);

    void removeNationalityChangeListener(NationalityChangeListener listener);
    
    void setImage(URI teamImage);
}
