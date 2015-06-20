package com.sap.sailing.domain.racelog.analyzing;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.regattalike.HasRegattaLike;

public class ServerSideRaceLogResolver implements RaceLogResolver {
    private final HasRegattaLike regattaLike;

    public ServerSideRaceLogResolver(HasRegattaLike regattaLike) {
        this.regattaLike = regattaLike;
    }

    @Override
    public RaceLog resolve(SimpleRaceLogIdentifier identifier) {
        final RaceLog result;
        if (regattaLike == null) {
            result = null;
        } else {
            result = regattaLike.getRacelog(identifier.getRaceColumnName(), identifier.getFleetName());
        }
        return result;
    }
}
