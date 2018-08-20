package com.sap.sailing.server.operationaltransformation;

import java.util.ArrayList;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.impl.DynamicCompetitorWithBoat;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.Util;

public class AllowCompetitorResetToDefaults extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = 5133140671156755328L;
    private final Iterable<String> competitorIdsAsStrings;
    
    public AllowCompetitorResetToDefaults(Iterable<String> competitorIdsAsStrings) {
        super();
        final ArrayList<String> arrayList = new ArrayList<>(); // to guarantee serializability, even if an unmodifiable or singleton is passed
        this.competitorIdsAsStrings = arrayList;
        Util.addAll(competitorIdsAsStrings, arrayList);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        final CompetitorAndBoatStore competitorStore = toState.getBaseDomainFactory().getCompetitorAndBoatStore();
        for (String competitorIdAsString : competitorIdsAsStrings) {
            Competitor competitor = competitorStore.getExistingCompetitorByIdAsString(competitorIdAsString);
            if (competitor != null) {
                competitorStore.allowCompetitorResetToDefaults(competitor);
                if (competitor.hasBoat()) {
                    competitorStore.allowBoatResetToDefaults(((DynamicCompetitorWithBoat) competitor).getBoat());
                }
            }
        }
        return null;
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
