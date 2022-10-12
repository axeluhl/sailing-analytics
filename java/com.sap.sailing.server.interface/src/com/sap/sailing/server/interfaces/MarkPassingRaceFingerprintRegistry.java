package com.sap.sailing.server.interfaces;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.markpassinghash.MarkPassingRaceFingerprint;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A registry to provide access to the {@link MarkPassingRaceFingerprint}s at the different levels of the domain model.
 * The following use cases exist:
 * <ul>
 * <li>Check for a {@link RaceIdentifier} whether mark passings have been stored persistently for the race identified by
 * that identifier; the result is a {@link MarkPassingRaceFingerprint} which can may or may not be
 * {@link MarkPassingRaceFingerprint#matches(com.sap.sailing.domain.tracking.TrackedRace) matched} by a
 * {@link TrackedRace}</li>
 * <li>If a matching fingerprint is stored in this registry for a race then {@link #loadMarkPassings(RaceIdentifier)
 * loading the mark passings} for that race and {@link DynamicTrackedRace#updateMarkPassings(Competitor, Iterable)
 * updating it to the tracked race} is the logical next step.</li>
 * <li>When a tracked race has reached a "stable" state, e.g., because it's {@link TrackedRace#getEndOfTracking()
 * end-of-tracking} time point has passed or its {@link TrackedRace#getStatus() status} has changed to
 * {@link TrackedRaceStatusEnum#FINISHED} then the latest mark passing calculation results can be
 * {@link #storeMarkPassings(RaceIdentifier, MarkPassingRaceFingerprint, Map) stored} together with the fingerprint
 * computed from the tracked race.</li>
 * </ul>
 *
 * @author Fabian Kallenbach (i550803)
 */
public interface MarkPassingRaceFingerprintRegistry {
    void storeMarkPassings(RaceIdentifier raceIdentifier, MarkPassingRaceFingerprint fingerprint, Map<Competitor, Map<Waypoint, MarkPassing>> markPassings);

    MarkPassingRaceFingerprint getMarkPassingRaceFingerprint(RaceIdentifier raceIdentifier);
    
    /**
     * <b>Precondition</b>: {@code fingerprint}.{@link MarkPassingRaceFingerprint#matches(com.sap.sailing.domain.tracking.TrackedRace) matches}
     * {@code (}{@link #getMarkPassingRaceFingerprint(RaceIdentifier) getMarkPassingRaceFingerprint(raceIdentifier)}{@code )}<p>
     * 
     * If a mark passing race fingerprint is known for {@code raceIdentifier} in this registry and the 
     */
    Map<Competitor, Map<Waypoint, MarkPassing>> loadMarkPassings(RaceIdentifier raceIdentifier);
}