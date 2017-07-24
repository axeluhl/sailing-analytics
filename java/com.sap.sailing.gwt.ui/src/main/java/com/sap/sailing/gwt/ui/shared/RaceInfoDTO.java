package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class RaceInfoDTO implements IsSerializable {
    public interface RaceInfoExtensionDTO extends IsSerializable {
        
    }
    
    public static class GateStartInfoDTO implements RaceInfoExtensionDTO {
        public String pathfinderId;
        public Long gateLineOpeningTime;
        
        // for GWT serialization
        public GateStartInfoDTO() { }
        
        public GateStartInfoDTO(String pathfinder, Long gateLineOpening) {
            this.pathfinderId = pathfinder;
            this.gateLineOpeningTime = gateLineOpening;
        }
    }
    
    public static class LineStartInfoDTO implements RaceInfoExtensionDTO {
        public Flags startModeFlag;
        
        // for GWT serialization
        public LineStartInfoDTO() { }
        
        public LineStartInfoDTO(Flags startMode) {
            this.startModeFlag = startMode;
        }
    }
    
    public RaceIdentifier raceIdentifier;
    public String raceName;
    public String fleetName;
    public int fleetOrdering;
    public String seriesName;
    public RaceLogRaceStatus lastStatus;
    public Date startTime;
    public Date finishedTime;
    public Date protestStartTime;
    public Date protestFinishTime;
    public Date lastUpdateTime;
    public Flags lastUpperFlag;
    public Flags lastLowerFlag;
    public boolean lastFlagsAreDisplayed;
    public boolean lastFlagsDisplayedStateChanged;
    public boolean isRaceAbortedInPassBefore;
    public Date abortingTimeInPassBefore;
    public boolean isTracked;
    public RaceCourseDTO lastCourseDesign;
    public String lastCourseName;
    public WindDTO lastWind;
    public RacingProcedureType startProcedure;
    public RaceInfoExtensionDTO startProcedureDTO;
    
    // for GWT serialization
    public RaceInfoDTO() { }
    
}
