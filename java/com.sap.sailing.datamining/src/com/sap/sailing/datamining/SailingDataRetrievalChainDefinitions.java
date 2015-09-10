package com.sap.sailing.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasMarkPassingContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasRaceResultOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.components.CompetitorOfRaceInLeaderboardRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.MarkPassingRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.RaceOfCompetitorRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedRaceRetrievalProcessor;
import com.sap.sailing.datamining.impl.data.LeaderboardGroupWithContext;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition.EmptySettings;

public class SailingDataRetrievalChainDefinitions {

    private final Collection<DataRetrieverChainDefinition<?, ?, ?>> dataRetrieverChainDefinitions;
    
    @SuppressWarnings("rawtypes")
    public SailingDataRetrievalChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();

        final DataRetrieverChainDefinition<RacingEventService, HasLeaderboardContext, EmptySettings> leaderboardRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                RacingEventService.class, HasLeaderboardContext.class, "LeaderboardSailingDomainRetrieverChain");
        leaderboardRetrieverChainDefinition.startWith(LeaderboardGroupRetrievalProcessor.class, LeaderboardGroupWithContext.class, "LeaderboardGroup");
        leaderboardRetrieverChainDefinition.endWith(LeaderboardGroupRetrievalProcessor.class, LeaderboardRetrievalProcessor.class,
                HasLeaderboardContext.class, "Leaderboard");
        
        final DataRetrieverChainDefinition<RacingEventService, HasRaceResultOfCompetitorContext, EmptySettings> raceResultOfCompetitorRetrieverChainDefinition =
                new SimpleDataRetrieverChainDefinition<>(leaderboardRetrieverChainDefinition, HasRaceResultOfCompetitorContext.class, "RaceResultSailingDomainRetrieverChain");
        raceResultOfCompetitorRetrieverChainDefinition.endWith(LeaderboardRetrievalProcessor.class, CompetitorOfRaceInLeaderboardRetrievalProcessor.class, HasRaceResultOfCompetitorContext.class, "Competitor");
        dataRetrieverChainDefinitions.add(raceResultOfCompetitorRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasTrackedRaceContext, EmptySettings> trackedRaceRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                leaderboardRetrieverChainDefinition, HasTrackedRaceContext.class, "RaceSailingDomainRetrieverChain");
        trackedRaceRetrieverChainDefinition.endWith(LeaderboardRetrievalProcessor.class, TrackedRaceRetrievalProcessor.class,
                HasTrackedRaceContext.class, "Race");

        final DataRetrieverChainDefinition<RacingEventService, HasMarkPassingContext, EmptySettings> markPassingRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                trackedRaceRetrieverChainDefinition, HasMarkPassingContext.class, "MarkPassingSailingDomainRetrieverChain");
        markPassingRetrieverChainDefinition.endWith(TrackedRaceRetrievalProcessor.class, MarkPassingRetrievalProcessor.class, HasMarkPassingContext.class, "MarkPassing");
        dataRetrieverChainDefinitions.add(markPassingRetrieverChainDefinition);
        
        final DataRetrieverChainDefinition<RacingEventService, HasRaceOfCompetitorContext, EmptySettings> raceOfCompetitorRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                trackedRaceRetrieverChainDefinition, HasRaceOfCompetitorContext.class, "RaceOfCompetitorSailingDomainRetrieverChain");
        raceOfCompetitorRetrieverChainDefinition.endWith(TrackedRaceRetrievalProcessor.class, RaceOfCompetitorRetrievalProcessor.class, HasRaceOfCompetitorContext.class, "Competitor");
        dataRetrieverChainDefinitions.add(raceOfCompetitorRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasTrackedLegOfCompetitorContext, EmptySettings> legOfCompetitorRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                trackedRaceRetrieverChainDefinition, HasTrackedLegOfCompetitorContext.class, "LegSailingDomainRetrieverChain");
        legOfCompetitorRetrieverChainDefinition.addAfter(TrackedRaceRetrievalProcessor.class, TrackedLegRetrievalProcessor.class, HasTrackedLegContext.class, "Leg");
        legOfCompetitorRetrieverChainDefinition.endWith(TrackedLegRetrievalProcessor.class, TrackedLegOfCompetitorRetrievalProcessor.class,
                HasTrackedLegOfCompetitorContext.class, "LegOfCompetitor");
        dataRetrieverChainDefinitions.add(legOfCompetitorRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasGPSFixContext, EmptySettings> gpsFixRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                legOfCompetitorRetrieverChainDefinition, HasGPSFixContext.class, "GPSFixSailingDomainRetrieverChain");
        gpsFixRetrieverChainDefinition.endWith(TrackedLegOfCompetitorRetrievalProcessor.class, GPSFixRetrievalProcessor.class,
                HasGPSFixContext.class, "GpsFix");
        dataRetrieverChainDefinitions.add(gpsFixRetrieverChainDefinition);
    }

    public Iterable<DataRetrieverChainDefinition<?, ?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }

}
