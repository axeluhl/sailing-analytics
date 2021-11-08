package com.sap.sailing.server.operationaltransformation;

import java.util.Collections;
import java.util.UUID;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class UpdateSpecificRegatta extends AbstractRacingEventServiceOperation<Regatta>{
    private static final long serialVersionUID = 8755035775682718882L;
    
    private final RegattaIdentifier regattaIdentifier;
    private final Iterable<UUID> newCourseAreaIds;
    private final RegattaConfiguration newConfiguration;
    private final Double buoyZoneRadiusInHullLengths;
    private final boolean useStartTimeInference;
    private final boolean controlTrackingFromStartAndFinishTimes;
    private final boolean autoRestartTrackingUponCompetitorSetChange;
    private final TimePoint startDate;
    private final TimePoint endDate;
    private final String registrationLinkSecret;
    private final CompetitorRegistrationType registrationType;

    /**
     * For backward compatibility: updates the regatta so that it has a single (if {@code newSingleCourseAreaId} is not {@code null})
     * or no (if {@code newSingleCourseAreaId} is {@code null}) course area defined.
     */
    public UpdateSpecificRegatta(RegattaIdentifier regattaIdentifier, TimePoint startDate, TimePoint endDate,
            UUID newSingleCourseAreaId, RegattaConfiguration newConfiguration, Double buoyZoneRadiusInHullLengths,
            boolean useStartTimeInference, boolean controlTrackingFromStartAndFinishTimes,
            boolean autoRestartTrackingUponCompetitorSetChange, String registrationLinkSecret, CompetitorRegistrationType registrationType) {
        this(regattaIdentifier, startDate, endDate,
                newSingleCourseAreaId == null ? Collections.emptySet() : Collections.singleton(newSingleCourseAreaId),
                newConfiguration, buoyZoneRadiusInHullLengths, useStartTimeInference,
                controlTrackingFromStartAndFinishTimes, autoRestartTrackingUponCompetitorSetChange,
                registrationLinkSecret, registrationType);
    }
    
    public UpdateSpecificRegatta(RegattaIdentifier regattaIdentifier, TimePoint startDate, TimePoint endDate,
            Iterable<UUID> newCourseAreaIds, RegattaConfiguration newConfiguration, Double buoyZoneRadiusInHullLengths,
            boolean useStartTimeInference, boolean controlTrackingFromStartAndFinishTimes,
            boolean autoRestartTrackingUponCompetitorSetChange, String registrationLinkSecret, CompetitorRegistrationType registrationType) {
        this.regattaIdentifier = regattaIdentifier;
        this.startDate = startDate;
        this.endDate = endDate;
        this.newCourseAreaIds = newCourseAreaIds;
        this.newConfiguration = newConfiguration;
        this.useStartTimeInference = useStartTimeInference;
        this.controlTrackingFromStartAndFinishTimes = controlTrackingFromStartAndFinishTimes;
        this.autoRestartTrackingUponCompetitorSetChange = autoRestartTrackingUponCompetitorSetChange;
        this.buoyZoneRadiusInHullLengths = buoyZoneRadiusInHullLengths;
        this.registrationLinkSecret = registrationLinkSecret;
        this.registrationType = registrationType;
    }

    @Override
    public Regatta internalApplyTo(RacingEventService toState) throws Exception {
        Regatta regatta = toState.updateRegatta(regattaIdentifier, startDate, endDate, newCourseAreaIds,
                newConfiguration, null, buoyZoneRadiusInHullLengths, useStartTimeInference,
                controlTrackingFromStartAndFinishTimes, autoRestartTrackingUponCompetitorSetChange, registrationLinkSecret, registrationType);
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
