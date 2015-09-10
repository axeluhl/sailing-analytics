package com.sap.sailing.polars.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.polars.datamining.components.PolarCompetitorRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarFleetRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarLeaderboardGroupRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarLeaderboardRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarLegRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarRaceColumnRetrievalProcessor;
import com.sap.sailing.polars.datamining.data.HasCompetitorPolarContext;
import com.sap.sailing.polars.datamining.data.HasFleetPolarContext;
import com.sap.sailing.polars.datamining.data.HasLeaderboardGroupPolarContext;
import com.sap.sailing.polars.datamining.data.HasLeaderboardPolarContext;
import com.sap.sailing.polars.datamining.data.HasLegPolarContext;
import com.sap.sailing.polars.datamining.data.HasRaceColumnPolarContext;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.DataRetrieverChainDefinitionWithSettings;

public class PolarsDataRetrievalChainDefinitions {
    
    private final Collection<DataRetrieverChainDefinition<?, ?, ?>> dataRetrieverChainDefinitions;
    
    public PolarsDataRetrievalChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();
        DataRetrieverChainDefinition<RacingEventService, HasCompetitorPolarContext, ?> definition1 = new DataRetrieverChainDefinitionWithSettings<RacingEventService, HasCompetitorPolarContext, PolarSheetGenerationSettings>(
                RacingEventService.class, HasCompetitorPolarContext.class, "PolarChain1",
                PolarSheetGenerationSettingsImpl.createStandardPolarSettings());
        definition1.startWith(PolarLeaderboardGroupRetrievalProcessor.class, HasLeaderboardGroupPolarContext.class,
                "LeaderboardGroup");
        definition1.addAfter(PolarLeaderboardGroupRetrievalProcessor.class, PolarLeaderboardRetrievalProcessor.class,
                HasLeaderboardPolarContext.class, "Leaderboard");
        definition1.addAfter(PolarLeaderboardRetrievalProcessor.class, PolarRaceColumnRetrievalProcessor.class,
                HasRaceColumnPolarContext.class, "RaceColumn");
        definition1.addAfter(PolarRaceColumnRetrievalProcessor.class, PolarFleetRetrievalProcessor.class,
                HasFleetPolarContext.class, "Fleet");
        definition1.addAfter(PolarFleetRetrievalProcessor.class, PolarLegRetrievalProcessor.class,
                HasLegPolarContext.class, "Leg");
        definition1.endWith(PolarLegRetrievalProcessor.class, PolarCompetitorRetrievalProcessor.class,
                HasCompetitorPolarContext.class, "Competitor");
        dataRetrieverChainDefinitions.add(definition1);
    }

    public Iterable<DataRetrieverChainDefinition<?, ?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }

}
