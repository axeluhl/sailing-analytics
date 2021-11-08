package com.sap.sailing.domain.swisstimingadapter;

import java.io.Serializable;
import java.util.List;

public interface Competitor extends Serializable {
    String getBoatID();
    
    String getThreeLetterIOCCode();
    
    List<CrewMember> getCrew();
    
    String getName();
    
    String getIdAsString();
}
