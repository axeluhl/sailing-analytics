package com.sap.sailing.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.RegattaLeaderboardRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedRaceRetrievalProcessor;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.SimpleDataRetrieverChainDefinition;

public class SailingDataRetrieverChainDefinitions {

    private final Collection<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions;
    
    public SailingDataRetrieverChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();

        DataRetrieverChainDefinition<RacingEventService, HasTrackedLegOfCompetitorContext> legOfCompetitorRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                RacingEventService.class, HasTrackedLegOfCompetitorContext.class, "SailingDomainRetrieverChain");
        @SuppressWarnings("unchecked")
        Class<Processor<RacingEventService, LeaderboardGroup>> leaderboardGroupRetrieverType = (Class<Processor<RacingEventService, LeaderboardGroup>>) (Class<?>) LeaderboardGroupRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.startWith(leaderboardGroupRetrieverType, LeaderboardGroup.class, "LeaderboardGroup");
        @SuppressWarnings("unchecked")
        Class<Processor<LeaderboardGroup, RegattaLeaderboard>> regattaLeaderboardRetrieverType = (Class<Processor<LeaderboardGroup, RegattaLeaderboard>>) (Class<?>) RegattaLeaderboardRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.addAfter(leaderboardGroupRetrieverType, regattaLeaderboardRetrieverType,
                RegattaLeaderboard.class, "RegattaLeaderboard");
        @SuppressWarnings("unchecked")
        Class<Processor<RegattaLeaderboard, HasTrackedRaceContext>> raceRetrieverType = (Class<Processor<RegattaLeaderboard, HasTrackedRaceContext>>) (Class<?>) TrackedRaceRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.addAfter(regattaLeaderboardRetrieverType, raceRetrieverType,
                HasTrackedRaceContext.class, "Race");
        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedRaceContext, HasTrackedLegContext>> legRetrieverType = (Class<Processor<HasTrackedRaceContext, HasTrackedLegContext>>) (Class<?>) TrackedLegRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.addAfter(raceRetrieverType, legRetrieverType, HasTrackedLegContext.class, "Leg");
        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedLegContext, HasTrackedLegOfCompetitorContext>> legOfCompetitorRetrieverType = (Class<Processor<HasTrackedLegContext, HasTrackedLegOfCompetitorContext>>) (Class<?>) TrackedLegOfCompetitorRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.endWith(legRetrieverType, legOfCompetitorRetrieverType,
                HasTrackedLegOfCompetitorContext.class, "LegOfCompetitor");
        dataRetrieverChainDefinitions.add(legOfCompetitorRetrieverChainDefinition);

        DataRetrieverChainDefinition<RacingEventService, HasGPSFixContext> gpsFixRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                legOfCompetitorRetrieverChainDefinition, HasGPSFixContext.class, "SailingDomainRetrieverChain");
        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedLegOfCompetitorContext, HasGPSFixContext>> gpsFixRetrieverType = (Class<Processor<HasTrackedLegOfCompetitorContext, HasGPSFixContext>>) (Class<?>) GPSFixRetrievalProcessor.class;
        gpsFixRetrieverChainDefinition.endWith(legOfCompetitorRetrieverType, gpsFixRetrieverType,
                HasGPSFixContext.class, "GpsFix");
        dataRetrieverChainDefinitions.add(gpsFixRetrieverChainDefinition);
    }

    public Collection<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }

}
