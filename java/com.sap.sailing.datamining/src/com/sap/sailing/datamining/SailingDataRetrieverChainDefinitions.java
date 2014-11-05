package com.sap.sailing.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.RegattaLeaderboardFilteringRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegFilteringRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorFilteringRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedRaceFilteringRetrievalProcessor;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.SimpleDataRetrieverChainDefinition;

public class SailingDataRetrieverChainDefinitions {

    private static Collection<DataRetrieverChainDefinition<?>> dataRetrieverChainDefinitions;
    
    public SailingDataRetrieverChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();

        DataRetrieverChainDefinition<RacingEventService> legOfCompetitorRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                RacingEventService.class, "SailingDomainRetrieverChain");
        @SuppressWarnings("unchecked")
        Class<Processor<RacingEventService, LeaderboardGroup>> leaderboardGroupRetrieverType = (Class<Processor<RacingEventService, LeaderboardGroup>>) (Class<?>) LeaderboardGroupRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.startWith(leaderboardGroupRetrieverType, LeaderboardGroup.class, "LeaderboardGroup");
        @SuppressWarnings("unchecked")
        Class<Processor<LeaderboardGroup, RegattaLeaderboard>> regattaLeaderboardRetrieverType = (Class<Processor<LeaderboardGroup, RegattaLeaderboard>>) (Class<?>) RegattaLeaderboardFilteringRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.addAfter(leaderboardGroupRetrieverType, regattaLeaderboardRetrieverType,
                RegattaLeaderboard.class, "RegattaLeaderboard");
        @SuppressWarnings("unchecked")
        Class<Processor<RegattaLeaderboard, HasTrackedRaceContext>> raceRetrieverType = (Class<Processor<RegattaLeaderboard, HasTrackedRaceContext>>) (Class<?>) TrackedRaceFilteringRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.addAfter(regattaLeaderboardRetrieverType, raceRetrieverType,
                HasTrackedRaceContext.class, "Race");
        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedRaceContext, HasTrackedLegContext>> legRetrieverType = (Class<Processor<HasTrackedRaceContext, HasTrackedLegContext>>) (Class<?>) TrackedLegFilteringRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.addAfter(raceRetrieverType, legRetrieverType, HasTrackedLegContext.class, "Leg");
        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedLegContext, HasTrackedLegOfCompetitorContext>> legOfCompetitorRetrieverType = (Class<Processor<HasTrackedLegContext, HasTrackedLegOfCompetitorContext>>) (Class<?>) TrackedLegOfCompetitorFilteringRetrievalProcessor.class;
        legOfCompetitorRetrieverChainDefinition.addAfter(legRetrieverType, legOfCompetitorRetrieverType,
                HasTrackedLegOfCompetitorContext.class, "LegOfCompetitor");
        dataRetrieverChainDefinitions.add(legOfCompetitorRetrieverChainDefinition);

        DataRetrieverChainDefinition<RacingEventService> gpsFixRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                legOfCompetitorRetrieverChainDefinition, "SailingDomainRetrieverChain");
        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedLegOfCompetitorContext, HasGPSFixContext>> gpsFixRetrieverType = (Class<Processor<HasTrackedLegOfCompetitorContext, HasGPSFixContext>>) (Class<?>) GPSFixRetrievalProcessor.class;
        gpsFixRetrieverChainDefinition.addAfter(legOfCompetitorRetrieverType, gpsFixRetrieverType,
                HasGPSFixContext.class, "GpsFix");
        dataRetrieverChainDefinitions.add(gpsFixRetrieverChainDefinition);
    }

    public Collection<DataRetrieverChainDefinition<?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }

}
