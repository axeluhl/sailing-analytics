package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * In addition to being able to resolve a race log from a {@link SimpleRaceLogIdentifier}, this
 * specialization can additionally look for a {@link TrackedRace} linked to the "slot" identified
 * by a {@link SimpleRaceLogIdentifier}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RaceLogAndTrackedRaceResolver extends RaceLogResolver {
    /**
     * The identifier is assumed to reference a {@link RaceColumn} in a {@link IsRegattaLike} object,
     * plus a {@link Fleet} object that can be used as an index into the {@link RaceColumn} object.
     * With this, both, the {@link RaceLog} as well as a {@link TrackedRace} can be looked up.
     */
    TrackedRace resolveTrackedRace(SimpleRaceLogIdentifier identifier);
}
