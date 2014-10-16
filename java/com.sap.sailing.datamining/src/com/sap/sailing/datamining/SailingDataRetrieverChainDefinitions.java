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

    public static Collection<DataRetrieverChainDefinition<?>> getDataRetrieverChainDefinitions() {
        if (dataRetrieverChainDefinitions == null) {
            initializeDataRetrieverChainDefinitions();
        }
        return dataRetrieverChainDefinitions;
    }

    private static void initializeDataRetrieverChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();

        DataRetrieverChainDefinition<RacingEventService> legRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(RacingEventService.class, "SailingDomainRetrieverChain");
        @SuppressWarnings("unchecked")
        Class<Processor<RacingEventService, LeaderboardGroup>> leaderboardGroupRetrieverType = (Class<Processor<RacingEventService, LeaderboardGroup>>)(Class<?>) LeaderboardGroupRetrievalProcessor.class;
        legRetrieverChainDefinition.startWith(leaderboardGroupRetrieverType, LeaderboardGroup.class);
        @SuppressWarnings("unchecked")
        Class<Processor<LeaderboardGroup, RegattaLeaderboard>> regattaLeaderboardRetrieverType = (Class<Processor<LeaderboardGroup, RegattaLeaderboard>>)(Class<?>) RegattaLeaderboardFilteringRetrievalProcessor.class;
        legRetrieverChainDefinition.addAsLast(leaderboardGroupRetrieverType, regattaLeaderboardRetrieverType, RegattaLeaderboard.class);
        @SuppressWarnings("unchecked")
        Class<Processor<RegattaLeaderboard, HasTrackedRaceContext>> raceRetrieverType = (Class<Processor<RegattaLeaderboard, HasTrackedRaceContext>>)(Class<?>) TrackedRaceFilteringRetrievalProcessor.class;
        legRetrieverChainDefinition.addAsLast(regattaLeaderboardRetrieverType, raceRetrieverType, HasTrackedRaceContext.class);
        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedRaceContext, HasTrackedLegContext>> legRetrieverType = (Class<Processor<HasTrackedRaceContext, HasTrackedLegContext>>)(Class<?>) TrackedLegFilteringRetrievalProcessor.class;
        legRetrieverChainDefinition.addAsLast(raceRetrieverType, legRetrieverType, HasTrackedLegContext.class);
        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedLegContext, HasTrackedLegOfCompetitorContext>> legOfCompetitorRetrieverType = (Class<Processor<HasTrackedLegContext, HasTrackedLegOfCompetitorContext>>)(Class<?>) TrackedLegOfCompetitorFilteringRetrievalProcessor.class;
        legRetrieverChainDefinition.addAsLast(legRetrieverType, legOfCompetitorRetrieverType, HasTrackedLegOfCompetitorContext.class);
        dataRetrieverChainDefinitions.add(legRetrieverChainDefinition);
        
        DataRetrieverChainDefinition<RacingEventService> gpsFixRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(legRetrieverChainDefinition, "SailingDomainRetrieverChain");
        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedLegOfCompetitorContext, HasGPSFixContext>> gpsFixRetrieverType = (Class<Processor<HasTrackedLegOfCompetitorContext, HasGPSFixContext>>)(Class<?>) GPSFixRetrievalProcessor.class;
        gpsFixRetrieverChainDefinition.addAsLast(legOfCompetitorRetrieverType, gpsFixRetrieverType, HasGPSFixContext.class);
        dataRetrieverChainDefinitions.add(gpsFixRetrieverChainDefinition);
    }

}
