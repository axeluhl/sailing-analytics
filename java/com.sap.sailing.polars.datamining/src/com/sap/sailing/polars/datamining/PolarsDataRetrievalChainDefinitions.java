package com.sap.sailing.polars.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.datamining.components.BackendPolarsBoatClassRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarCompetitorRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarFleetRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarGPSFixRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarLeaderboardGroupRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarLeaderboardRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarLegRetrievalProcessor;
import com.sap.sailing.polars.datamining.components.PolarRaceColumnRetrievalProcessor;
import com.sap.sailing.polars.datamining.data.HasBackendPolarBoatClassContext;
import com.sap.sailing.polars.datamining.data.HasCompetitorPolarContext;
import com.sap.sailing.polars.datamining.data.HasFleetPolarContext;
import com.sap.sailing.polars.datamining.data.HasGPSFixPolarContext;
import com.sap.sailing.polars.datamining.data.HasLeaderboardGroupPolarContext;
import com.sap.sailing.polars.datamining.data.HasLeaderboardPolarContext;
import com.sap.sailing.polars.datamining.data.HasLegPolarContext;
import com.sap.sailing.polars.datamining.data.HasRaceColumnPolarContext;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettings;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettingsImpl;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.SingleDataRetrieverChainDefinition;

/**
 * Builds the polar related retriever chains.
 *
 * </br></br>
 * 
 * At the time of writing there are two chains, one for actual generation of custom polars and one for simple access of
 * backend saved polar in the {@link PolarDataService}
 * 
 * @author D054528 (Frederik Petersen)
 *
 */
public class PolarsDataRetrievalChainDefinitions {
    
    private final Collection<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions;
    
    public PolarsDataRetrievalChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();
        DataRetrieverChainDefinition<RacingEventService, HasGPSFixPolarContext> definition1 = new SimpleDataRetrieverChainDefinition<>(
                RacingEventService.class, HasGPSFixPolarContext.class, "PolarChain1");
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
        definition1.addAfter(PolarLegRetrievalProcessor.class, PolarCompetitorRetrievalProcessor.class,
                HasCompetitorPolarContext.class, "Competitor");
        definition1.endWith(PolarCompetitorRetrievalProcessor.class, PolarGPSFixRetrievalProcessor.class,
                HasGPSFixPolarContext.class, PolarDataMiningSettings.class, PolarDataMiningSettingsImpl.createStandardPolarSettings(), "GPSFix");
        dataRetrieverChainDefinitions.add(definition1);
        
        DataRetrieverChainDefinition<RacingEventService, HasBackendPolarBoatClassContext> definition2 = new SingleDataRetrieverChainDefinition<>(
                RacingEventService.class, HasBackendPolarBoatClassContext.class, "PolarChain2");
        definition2.startWith(BackendPolarsBoatClassRetrievalProcessor.class, HasBackendPolarBoatClassContext.class, "BoatClass");
     
        dataRetrieverChainDefinitions.add(definition2);
    }

    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }

}
