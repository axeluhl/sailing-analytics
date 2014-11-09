package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateSpecificRegatta extends AbstractRacingEventServiceOperation<Regatta>{
    private static final long serialVersionUID = 8755035775682718882L;
    
    private final RegattaIdentifier regattaIdentifier;
    private final UUID newDefaultCourseAreaId;
    private final RegattaConfiguration newConfiguration;
    private final boolean useStartTimeInference;
    
    public UpdateSpecificRegatta(RegattaIdentifier regattaIdentifier, UUID newDefaultCourseAreaId,
            RegattaConfiguration newConfiguration, boolean useStartTimeInference) {
        this.regattaIdentifier = regattaIdentifier;
        this.newDefaultCourseAreaId = newDefaultCourseAreaId;
        this.newConfiguration = newConfiguration;
        this.useStartTimeInference = useStartTimeInference;
    }

    @Override
    public Regatta internalApplyTo(RacingEventService toState) throws Exception {
        Regatta regatta = toState.updateRegatta(regattaIdentifier, newDefaultCourseAreaId, newConfiguration, null, useStartTimeInference);
        return regatta;
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
