package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore.CompetitorUpdateListener;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public class CreateCompetitor extends AbstractRacingEventServiceOperation<Competitor> {
    private static final long serialVersionUID = 1172181354320184263L;
    private final Serializable competitorId;
    private final String name;
    private final String shortName;
    private final Color displayColor;
    private final String email;
    private final URI flagImageUri;
    private final Nationality nationality;
    private final Double timeOnTimeFactor;
    private final Duration timeOnDistanceAllowancePerNauticalMile;
    private final String searchTag;
    
    public CreateCompetitor(Serializable competitorId, String name, String shortName, Color displayColor,
            String email, URI flagImageUri, Nationality nationality,
            Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        super();
        this.competitorId = competitorId;
        this.name = name;
        this.shortName = shortName;
        this.displayColor = displayColor;
        this.email = email;
        this.flagImageUri = flagImageUri;
        this.nationality = nationality;
        this.timeOnTimeFactor = timeOnTimeFactor;
        this.timeOnDistanceAllowancePerNauticalMile = timeOnDistanceAllowancePerNauticalMile;
        this.searchTag = searchTag;
    }

    /**
     * {@link #internalApplyTo(RacingEventService)} already replicates the effects; see {@link RacingEventServiceImpl#RacingEventServiceImpl()}
     * where a {@link CompetitorUpdateListener} is registered that handles replication of competitor creations
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    @Override
    public Competitor internalApplyTo(RacingEventService toState) throws Exception {
        DynamicPerson sailor = new PersonImpl(name, nationality, null, null);
        DynamicTeam team = new TeamImpl(name + " team", Collections.singleton(sailor), null);
        final Competitor result = toState.getBaseDomainFactory().getCompetitorStore()
                .getOrCreateCompetitor(competitorId, name, shortName, displayColor, email, flagImageUri,
                        team, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
        return result;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
