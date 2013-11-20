package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Team;

public interface DynamicTeam extends Team {
    void setNationality(Nationality newNationality);
}
