package com.sap.sailing.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasMarkPassingContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.MarkPassingRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.RegattaLeaderboardRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedRaceRetrievalProcessor;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.SimpleDataRetrieverChainDefinition;

public class SailingDataRetrievalChainDefinitions {

    private final Collection<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions;
    
    public SailingDataRetrievalChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();

        final DataRetrieverChainDefinition<RacingEventService, HasTrackedLegOfCompetitorContext> trackedRaceRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                RacingEventService.class, HasTrackedLegOfCompetitorContext.class, "RaceSailingDomainRetrieverChain");
        trackedRaceRetrieverChainDefinition.startWith(LeaderboardGroupRetrievalProcessor.class, LeaderboardGroup.class, "LeaderboardGroup");
        trackedRaceRetrieverChainDefinition.addAfter(LeaderboardGroupRetrievalProcessor.class, RegattaLeaderboardRetrievalProcessor.class,
                RegattaLeaderboard.class, "RegattaLeaderboard");
        trackedRaceRetrieverChainDefinition.addAfter(RegattaLeaderboardRetrievalProcessor.class, TrackedRaceRetrievalProcessor.class,
                HasTrackedRaceContext.class, "Race");

        final DataRetrieverChainDefinition<RacingEventService, HasMarkPassingContext> markPassingRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                trackedRaceRetrieverChainDefinition, HasMarkPassingContext.class, "MarkPassingSailingDomainRetrieverChain");
        markPassingRetrieverChainDefinition.endWith(TrackedRaceRetrievalProcessor.class, MarkPassingRetrievalProcessor.class, HasMarkPassingContext.class, "MarkPassing");
        dataRetrieverChainDefinitions.add(markPassingRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasTrackedLegOfCompetitorContext> legOfCompetitorRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                trackedRaceRetrieverChainDefinition, HasTrackedLegOfCompetitorContext.class, "LegSailingDomainRetrieverChain");
        legOfCompetitorRetrieverChainDefinition.addAfter(TrackedRaceRetrievalProcessor.class, TrackedLegRetrievalProcessor.class, HasTrackedLegContext.class, "Leg");
        legOfCompetitorRetrieverChainDefinition.endWith(TrackedLegRetrievalProcessor.class, TrackedLegOfCompetitorRetrievalProcessor.class,
                HasTrackedLegOfCompetitorContext.class, "LegOfCompetitor");
        dataRetrieverChainDefinitions.add(legOfCompetitorRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasGPSFixContext> gpsFixRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                legOfCompetitorRetrieverChainDefinition, HasGPSFixContext.class, "GPSFixSailingDomainRetrieverChain");
        gpsFixRetrieverChainDefinition.endWith(TrackedLegOfCompetitorRetrievalProcessor.class, GPSFixRetrievalProcessor.class,
                HasGPSFixContext.class, "GpsFix");
        dataRetrieverChainDefinitions.add(gpsFixRetrieverChainDefinition);
    }

    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }

}
