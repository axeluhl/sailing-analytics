package com.sap.sailing.server.security;

import java.util.Optional;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceTrackingHandler.DefaultRaceTrackingHandler;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.util.ThreadLocalTransporter;

public class PermissionAwareRaceTrackingHandler extends DefaultRaceTrackingHandler {

    private final Subject subject;
    private final SecurityService securityService;

    public PermissionAwareRaceTrackingHandler(SecurityService securityService) {
        this.securityService = securityService;
        subject = SecurityUtils.getSubject();
    }

    @Override
    public DynamicTrackedRace createTrackedRace(TrackedRegatta trackedRegatta, RaceDefinition raceDefinition,
            Iterable<Sideline> sidelines, WindStore windStore, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, boolean useMarkPassingCalculator,
            RaceLogResolver raceLogResolver, Optional<ThreadLocalTransporter> threadLocalTransporter) {

        SubjectThreadState subjectThreadState = new SubjectThreadState(subject);
        subjectThreadState.bind();
        try {
            RegattaNameAndRaceName regattaAndRaceIdentifier = new RegattaNameAndRaceName(
                    trackedRegatta.getRegatta().getName(), raceDefinition.getName());
            QualifiedObjectIdentifier qualifiedObjectIdentifier = TrackedRace.getIdentifier(regattaAndRaceIdentifier);
            return securityService.setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                    SecuredDomainType.TRACKED_RACE, qualifiedObjectIdentifier.getTypeRelativeObjectIdentifier(),
                    regattaAndRaceIdentifier.toString(), () -> {
                        return super.createTrackedRace(trackedRegatta, raceDefinition, sidelines, windStore,
                                delayToLiveInMillis, millisecondsOverWhichToAverageWind,
                                millisecondsOverWhichToAverageSpeed, raceDefinitionSetToUpdate,
                                useMarkPassingCalculator, raceLogResolver, threadLocalTransporter);
                    });
        } finally {
            subjectThreadState.clear();
        }
    }

}
