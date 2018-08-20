package com.sap.sailing.domain.swisstimingadapter;

import java.util.List;

public interface Competitor {
    String getBoatID();
    
    String getThreeLetterIOCCode();
    
    List<CrewMember> getCrew();
    
    String getName();
    
    String getIdAsString();
}
