package com.sap.sailing.domain.racelog.analyzing;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException;
import com.sap.sailing.domain.regattalike.HasRegattaLike;

public class ServerSideRaceLogResolver implements RaceLogResolver {

    private HasRegattaLike regattaLike;

    public ServerSideRaceLogResolver(HasRegattaLike regattaLike) {
        this.regattaLike = regattaLike;
    }
    
    @Override
    public RaceLog resolve(SimpleRaceLogIdentifier identifier) throws RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException {
        if (regattaLike == null){
            return null;
        }
        
        return regattaLike.getRacelog(identifier);
    }

}
