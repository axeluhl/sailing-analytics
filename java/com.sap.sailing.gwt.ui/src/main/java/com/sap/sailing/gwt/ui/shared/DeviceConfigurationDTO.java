package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class DeviceConfigurationDTO implements IsSerializable {
    
    public static class RegattaConfigurationDTO implements IsSerializable {
        public static class RRS26ConfigurationDTO extends RacingProcedureConfigurationDTO {
            public List<Flags> startModeFlags;
        }
        
        public static class GateStartConfigurationDTO extends RacingProcedureConfigurationDTO {
            public Boolean hasPathfinder;
            public Boolean hasAdditionalGolfDownTime;
        }
        
        public static class ESSConfigurationDTO extends RacingProcedureConfigurationDTO {
            
        }

        public static class LeagueConfigurationDTO extends RacingProcedureConfigurationDTO {
            
        }

        public static class RacingProcedureConfigurationDTO implements IsSerializable {
            public Flags classFlag;
            public Boolean hasInidividualRecall;
        }
        public RacingProcedureType defaultRacingProcedureType;
        public CourseDesignerMode defaultCourseDesignerMode;
        
        public RRS26ConfigurationDTO rrs26Configuration;
        public GateStartConfigurationDTO gateStartConfiguration;
        public ESSConfigurationDTO essConfiguration;
        public RacingProcedureConfigurationDTO basicConfiguration;
        public LeagueConfigurationDTO leagueConfiguration;
    }

    public List<String> allowedCourseAreaNames;
    public String resultsMailRecipient;
    public List<String> byNameDesignerCourseNames;
    public RegattaConfigurationDTO regattaConfiguration;
}
