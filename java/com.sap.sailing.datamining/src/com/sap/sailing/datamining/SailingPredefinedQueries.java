package com.sap.sailing.datamining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
    
    private final StatisticQueryDefinitionDTO create_AvgSpeed_49erEuros2015_CompetitorName_LegType() {
        FunctionDTO statistic = new FunctionDTO(false, "getGPSFix().getSpeed().getKnots()", "com.sap.sailing.datamining.data.HasGPSFixContext", "double", new ArrayList<String>(), "", 0);
        AggregationProcessorDefinitionDTO aggregator = new AggregationProcessorDefinitionDTO("Average", "java.lang.Number", "java.lang.Number", "");

        ArrayList<DataRetrieverLevelDTO> retrieverLevels = new ArrayList<>();
        retrieverLevels.add(new DataRetrieverLevelDTO(0, "com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor", new LocalizedTypeDTO("com.sap.sailing.datamining.impl.data.LeaderboardGroupWithContext", "Leaderboard Group"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(1, "com.sap.sailing.datamining.impl.components.LeaderboardRetrievalProcessor", new LocalizedTypeDTO("com.sap.sailing.datamining.data.HasLeaderboardContext", "Leaderboard"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(2, "com.sap.sailing.datamining.impl.components.TrackedRaceRetrievalProcessor", new LocalizedTypeDTO("com.sap.sailing.datamining.data.HasTrackedRaceContext", "Race"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(3, "com.sap.sailing.datamining.impl.components.TrackedLegRetrievalProcessor", new LocalizedTypeDTO("com.sap.sailing.datamining.data.HasTrackedLegContext", "Leg"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(4, "com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorRetrievalProcessor", new LocalizedTypeDTO("com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext", "Leg of competitor"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(5, "com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor", new LocalizedTypeDTO("com.sap.sailing.datamining.data.HasGPSFixContext", "GPS-Fix"), null));
        DataRetrieverChainDefinitionDTO retrieverChain = new DataRetrieverChainDefinitionDTO("", "com.sap.sailing.server.RacingEventService", retrieverLevels);

        ModifiableStatisticQueryDefinitionDTO queryDefinition = new ModifiableStatisticQueryDefinitionDTO("default", statistic, aggregator, retrieverChain);

        HashMap<FunctionDTO, HashSet<? extends Serializable>> retrieverlevel2_FilterSelection = new HashMap<>();
        FunctionDTO filterDimension0 = new FunctionDTO(true, "getRegatta().getName()", "com.sap.sailing.datamining.data.HasTrackedRaceContext", "java.lang.String", new ArrayList<String>(), "", 0);
        HashSet<Serializable> filterDimension0_Selection = new HashSet<>();
        filterDimension0_Selection.add("49er Euros 2015");
        retrieverlevel2_FilterSelection.put(filterDimension0, filterDimension0_Selection);
        queryDefinition.setFilterSelectionFor(retrieverChain.getRetrieverLevel(2), retrieverlevel2_FilterSelection);

        FunctionDTO dimensionToGroupBy0 = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getCompetitor().getTeam().getName()", "com.sap.sailing.datamining.data.HasGPSFixContext", "java.lang.String", new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy0);

        FunctionDTO dimensionToGroupBy1 = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getTrackedLegContext().getLegType()", "com.sap.sailing.datamining.data.HasGPSFixContext", "com.sap.sailing.domain.common.LegType", new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy1);
        
        return queryDefinition;
    }

    public Map<String, StatisticQueryDefinitionDTO> get() {
        return predefinedQueriesMappedByName;
    }

}
