package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public class RaceInfoDTO implements IsSerializable {
    public String raceName;
    public String fleetName;
    public int fleetOrdering;
    public Date startTime;
    public Date finishedTime;
    public Date lastUpdateTime;
    public RaceLogRaceStatus lastStatus;
    public Flags lastUpperFlag;
    public Flags lastLowerFlag;
    public boolean isLastFlagDisplayed;
    public boolean isTracked;
    public RaceCourseDTO lastCourseDesign;
    public String lastCourseName;
    public RaceIdentifier raceIdentifier;
    public String pathfinderId;
    public Long gateLineOpeningTime;
    public boolean isRaceAbortedInPassBefore;
    public String seriesName;
    public Date protestFinishTime;
    
    // for GWT serialization
    public RaceInfoDTO() { }
    
}
