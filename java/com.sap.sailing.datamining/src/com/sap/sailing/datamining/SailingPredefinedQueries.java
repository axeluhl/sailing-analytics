package com.sap.sailing.datamining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedRaceRetrievalProcessor;
import com.sap.sailing.datamining.impl.data.LeaderboardGroupWithContext;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;

public class SailingPredefinedQueries {

    private final Map<String, StatisticQueryDefinitionDTO> predefinedQueriesMappedByName;
    
    public SailingPredefinedQueries() {
        predefinedQueriesMappedByName = new HashMap<>();

        String factTypeName = "com.sap.sailing.datamining.data.HasGPSFixContext";
        
        FunctionDTO gpsFixSpeed = new FunctionDTO(false, "getGPSFix().getSpeed().getKnots()", factTypeName, "double", new ArrayList<String>(), "", 0);
        AggregationProcessorDefinitionDTO numberAverage = new AggregationProcessorDefinitionDTO("Average", Number.class.getName(), Number.class.getName(), "");
        
        String racingEventServiceTypeName = "com.sap.sailing.server.RacingEventService";
        ArrayList<DataRetrieverLevelDTO> retrieverLevels = new ArrayList<>();
        retrieverLevels.add(new DataRetrieverLevelDTO(0, "com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor",
                                                      new LocalizedTypeDTO("", ""), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(1, "com.sap.sailing.datamining.impl.components.LeaderboardRetrievalProcessor",
                                                      new LocalizedTypeDTO("", ""), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(2, "com.sap.sailing.datamining.impl.components.TrackedRaceRetrievalProcessor",
                                                      new LocalizedTypeDTO("", ""), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(3, "com.sap.sailing.datamining.impl.components.TrackedLegRetrievalProcessor",
                                                      new LocalizedTypeDTO("", ""), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(4, "com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorRetrievalProcessor",
                                                      new LocalizedTypeDTO("", ""), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(5, "com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor",
                                                      new LocalizedTypeDTO(factTypeName, ""), null));
        DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition = new DataRetrieverChainDefinitionDTO("", racingEventServiceTypeName, retrieverLevels);
        
        ModifiableStatisticQueryDefinitionDTO avgSpeed_49erEuros2015_CompetitorName_LegType =
                new ModifiableStatisticQueryDefinitionDTO("default", gpsFixSpeed, numberAverage, dataRetrieverChainDefinition);
        
        HashMap<FunctionDTO, HashSet<? extends Serializable>> levelFilterSelection = new HashMap<>();
        String sourceTypeName = "com.sap.sailing.datamining.data.HasTrackedRaceContext";
        String returnTypeName = String.class.getName();
        FunctionDTO regattaName = new FunctionDTO(true, "getRegatta().getName()", sourceTypeName, returnTypeName, new ArrayList<String>(), "", 0);
        HashSet<Serializable> filterSelection = new HashSet<>();
        filterSelection.add("49er Euros 2015");
        levelFilterSelection.put(regattaName, filterSelection);
        avgSpeed_49erEuros2015_CompetitorName_LegType.setFilterSelectionFor(dataRetrieverChainDefinition.getRetrieverLevel(2), levelFilterSelection);
        
        returnTypeName = String.class.getName();
        FunctionDTO competitorName = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getCompetitor().getTeam().getName()", factTypeName, returnTypeName, new ArrayList<String>(), "", 0);
        avgSpeed_49erEuros2015_CompetitorName_LegType.appendDimensionToGroupBy(competitorName);
        
        returnTypeName = "com.sap.sailing.domain.common.LegType";
        FunctionDTO legType = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getTrackedLegContext().getLegType()", factTypeName, returnTypeName, new ArrayList<String>(), "", 0);
        avgSpeed_49erEuros2015_CompetitorName_LegType.appendDimensionToGroupBy(legType);

        predefinedQueriesMappedByName.put("49er Euros 2015: Average Speed grouped by Competitor and Leg Type", avgSpeed_49erEuros2015_CompetitorName_LegType);
        predefinedQueriesMappedByName.put("49er Euros 2015: Average Speed grouped by Competitor and Leg Type - Generated", create_AvgSpeed_49erEuros2015_CompetitorName_LegType());
    }
    
    private StatisticQueryDefinitionDTO create_AvgSpeed_49erEuros2015_CompetitorName_LegType() {
        FunctionDTO statistic = new FunctionDTO(false, "getGPSFix().getSpeed().getKnots()", HasGPSFixContext.class.getName(), double.class.getName(), new ArrayList<String>(), "", 0);
        AggregationProcessorDefinitionDTO aggregator = new AggregationProcessorDefinitionDTO("Average", Number.class.getName(), Number.class.getName(), "");

        ArrayList<DataRetrieverLevelDTO> retrieverLevels = new ArrayList<>();
        retrieverLevels.add(new DataRetrieverLevelDTO(0, LeaderboardGroupRetrievalProcessor.class.getName(), new LocalizedTypeDTO(LeaderboardGroupWithContext.class.getName(), "Leaderboard Group"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(1, LeaderboardRetrievalProcessor.class.getName(), new LocalizedTypeDTO(HasLeaderboardContext.class.getName(), "Leaderboard"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(2, TrackedRaceRetrievalProcessor.class.getName(), new LocalizedTypeDTO(HasTrackedRaceContext.class.getName(), "Race"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(3, TrackedLegRetrievalProcessor.class.getName(), new LocalizedTypeDTO(HasTrackedLegContext.class.getName(), "Leg"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(4, TrackedLegOfCompetitorRetrievalProcessor.class.getName(), new LocalizedTypeDTO(HasTrackedLegOfCompetitorContext.class.getName(), "Leg of competitor"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(5, GPSFixRetrievalProcessor.class.getName(), new LocalizedTypeDTO(HasGPSFixContext.class.getName(), "GPS-Fix"), null));
        DataRetrieverChainDefinitionDTO retrieverChain = new DataRetrieverChainDefinitionDTO("", RacingEventService.class.getName(), retrieverLevels);

        ModifiableStatisticQueryDefinitionDTO queryDefinition = new ModifiableStatisticQueryDefinitionDTO("default", statistic, aggregator, retrieverChain);

        FunctionDTO dimensionToGroupBy0 = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getCompetitor().getTeam().getName()", HasGPSFixContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy0);

        FunctionDTO dimensionToGroupBy1 = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getTrackedLegContext().getLegType()", HasGPSFixContext.class.getName(), LegType.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy1);
        
        return queryDefinition;
    }

    public Map<String, StatisticQueryDefinitionDTO> get() {
        return predefinedQueriesMappedByName;
    }

}
