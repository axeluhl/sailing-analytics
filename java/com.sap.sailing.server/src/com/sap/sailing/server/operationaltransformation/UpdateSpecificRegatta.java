package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class UpdateSpecificRegatta extends AbstractRacingEventServiceOperation<Regatta>{
    private static final long serialVersionUID = 8755035775682718882L;
    
    private final RegattaIdentifier regattaIdentifier;
    private final UUID newDefaultCourseAreaId;
    private final RegattaConfiguration newConfiguration;
    private final Double buoyZoneRadiusInHullLengths;
    private final boolean useStartTimeInference;
    private final boolean controlTrackingFromStartAndFinishTimes;
    private final TimePoint startDate;
    private final TimePoint endDate;
    private final String registrationLinkSecret;
    
    public UpdateSpecificRegatta(RegattaIdentifier regattaIdentifier, TimePoint startDate, TimePoint endDate,
            UUID newDefaultCourseAreaId, RegattaConfiguration newConfiguration, Double buoyZoneRadiusInHullLengths,
            boolean useStartTimeInference, boolean controlTrackingFromStartAndFinishTimes,
            String registrationLinkSecret) {
        this.regattaIdentifier = regattaIdentifier;
        this.startDate = startDate;
        this.endDate = endDate;
        this.newDefaultCourseAreaId = newDefaultCourseAreaId;
        this.newConfiguration = newConfiguration;
        this.useStartTimeInference = useStartTimeInference;
        this.controlTrackingFromStartAndFinishTimes = controlTrackingFromStartAndFinishTimes;
        this.buoyZoneRadiusInHullLengths = buoyZoneRadiusInHullLengths;
        this.registrationLinkSecret = registrationLinkSecret;
    }

    @Override
    public Regatta internalApplyTo(RacingEventService toState) throws Exception {
        Regatta regatta = toState.updateRegatta(regattaIdentifier, startDate, endDate, newDefaultCourseAreaId, newConfiguration, null, buoyZoneRadiusInHullLengths, useStartTimeInference, controlTrackingFromStartAndFinishTimes, registrationLinkSecret);
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
