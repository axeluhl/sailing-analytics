package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public class RaceInfoDTO implements IsSerializable {
    public String raceName;
    public String fleet;
    public Date startTime;
    public RaceLogRaceStatus lastStatus;
    public Flags lastUpperFlag;
    public Flags lastLowerFlag;
    public boolean displayed;
    public RaceCourseDTO lastCourseDesign;
    public RaceIdentifier raceIdentifier;
    public String additionalInformation;
    
    // for GWT serialization
    public RaceInfoDTO() { }
    
}
