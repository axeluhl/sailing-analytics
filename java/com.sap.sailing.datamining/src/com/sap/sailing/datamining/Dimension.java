package com.sap.sailing.datamining;

import com.sap.sailing.datamining.shared.GroupKey;

public interface Dimension extends GroupKey {
    
    public enum Type {
        
        RegattaName, RaceName, LegNumber,
        CourseArea,
        Fleet,
        BoatClassName,
        Year,
        LegType,
        CompetitorName,
        SailID,
        Nationality,
        WindStrength

    }
    
    public Type getType();

}
