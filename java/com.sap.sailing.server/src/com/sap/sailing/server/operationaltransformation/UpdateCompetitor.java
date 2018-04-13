package com.sap.sailing.server.operationaltransformation;

import java.net.URI;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.CompetitorAndBoatStore.CompetitorUpdateListener;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public class UpdateCompetitor extends AbstractRacingEventServiceOperation<Competitor> {
    private static final long serialVersionUID = 1172181354320184263L;
    private final String idAsString;
    private final String newName;
    private final String newShortName;
    private final Color newDisplayColor;
    private final String newEmail;
    private final Nationality newNationality;
    private final URI newTeamImageUri;
    private final URI newFlagImageUri;
    private final Double timeOnTimeFactor;
    private final Duration timeOnDistanceAllowancePerNauticalMile;
    private final String newSearchTag;
    
    /**
     * @param idAsString
     *            Identified the competitor to update
     * @param newNationality
     *            if <code>null</code>, the competitor obtains the "NONE" nationality, usually represented by a white
     *            flag
     */
    public UpdateCompetitor(String idAsString, String newName, String newShortName, Color newDisplayColor, String newEmail,
            Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri,
            Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String newSearchTag) {
        super();
        this.idAsString = idAsString;
        this.newName = newName;
        this.newShortName = newShortName; 
        this.newDisplayColor = newDisplayColor;
        this.newNationality = newNationality;
        this.newTeamImageUri = newTeamImageUri;
        this.newEmail = newEmail;
        this.newFlagImageUri = newFlagImageUri;
        this.timeOnTimeFactor = timeOnTimeFactor;
        this.timeOnDistanceAllowancePerNauticalMile = timeOnDistanceAllowancePerNauticalMile;
        this.newSearchTag = newSearchTag;
    }

    /**
     * {@link #internalApplyTo(RacingEventService)} already replicates the effects; see {@link RacingEventServiceImpl#RacingEventServiceImpl()}
     * where a {@link CompetitorUpdateListener} is registered that handles replication of any updates to a competitor.
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    @Override
    public Competitor internalApplyTo(RacingEventService toState) throws Exception {
        Competitor result = toState.getBaseDomainFactory().getCompetitorAndBoatStore()
                .updateCompetitor(idAsString, newName, newShortName, newDisplayColor, newEmail, newNationality,
                        newTeamImageUri, newFlagImageUri, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, newSearchTag);
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
