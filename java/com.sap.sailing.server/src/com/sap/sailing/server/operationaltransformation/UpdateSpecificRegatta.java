package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateSpecificRegatta extends AbstractRacingEventServiceOperation<Regatta>{
    private static final long serialVersionUID = 8755035775682718882L;
    
    private final RegattaIdentifier regattaIdentifier;
    private final Serializable newDefaultCourseAreaId;
    private final RacingProcedureType newDefaultRacingProcedureType;
    private final CourseDesignerMode newDefaultCourseDesignerMode;
    private final RegattaConfiguration newConfiguration;
    
    public UpdateSpecificRegatta(RegattaIdentifier regattaIdentifier, Serializable newDefaultCourseAreaId,
            RacingProcedureType newDefaultRacingProcedureType, CourseDesignerMode newDefaultCourseDesignerMode,
            RegattaConfiguration newConfiguration) {
        this.regattaIdentifier = regattaIdentifier;
        this.newDefaultCourseAreaId = newDefaultCourseAreaId;
        this.newDefaultRacingProcedureType = newDefaultRacingProcedureType;
        this.newDefaultCourseDesignerMode = newDefaultCourseDesignerMode;
        this.newConfiguration = newConfiguration;
    }

    @Override
    public Regatta internalApplyTo(RacingEventService toState) throws Exception {
        return toState.updateRegatta(regattaIdentifier, newDefaultCourseAreaId, 
                newDefaultRacingProcedureType, newDefaultCourseDesignerMode,
                newConfiguration);
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }

}
