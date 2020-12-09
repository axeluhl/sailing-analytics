package com.sap.sailing.gwt.ui.shared;

import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.Duration;

public class DeviceConfigurationDTO implements IsSerializable {
    public static class RegattaConfigurationDTO implements IsSerializable {
        public static class RacingProcedureWithConfigurableStartModeFlagConfigurationDTO extends RacingProcedureConfigurationDTO {
            public List<Flags> startModeFlags;
        }
        
        public static class RRS26ConfigurationDTO extends RacingProcedureWithConfigurableStartModeFlagConfigurationDTO {
        }

        public static class SWCStartConfigurationDTO extends RacingProcedureWithConfigurableStartModeFlagConfigurationDTO {
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
            public Boolean hasIndividualRecall;
            public Boolean isResultEntryEnabled;
        }
        public RacingProcedureType defaultRacingProcedureType;
        public CourseDesignerMode defaultCourseDesignerMode;
        public Duration defaultProtestTimeDuration;
        
        public RRS26ConfigurationDTO rrs26Configuration;
        public SWCStartConfigurationDTO swcStartConfiguration;
        public GateStartConfigurationDTO gateStartConfiguration;
        public ESSConfigurationDTO essConfiguration;
        public RacingProcedureConfigurationDTO basicConfiguration;
        public LeagueConfigurationDTO leagueConfiguration;
    }

    public String name;
    
    public UUID id;

    /**
     * may be {@code null}
     */
    public List<String> allowedCourseAreaNames;

    public String resultsMailRecipient;

    /**
     * may be {@code null}
     */
    public List<String> byNameDesignerCourseNames;
    
    public RegattaConfigurationDTO regattaConfiguration;
    
    /**
     * An optional ID of an event to automatically select from the events list during the logon process
     */
    public UUID eventId;
    
    /**
     * An optional ID of a course area if an {@link #eventId} has been provided; the course area ID then is expected
     * to be chosen from one of the event venue's course areas.
     */
    public UUID courseAreaId;
    
    public int priority;
}
