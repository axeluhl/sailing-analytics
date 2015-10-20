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
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;

public class SailingPredefinedQueries {

    private final Map<PredefinedQueryIdentifier, StatisticQueryDefinitionDTO> predefinedQueries;
    
    public SailingPredefinedQueries() {
        predefinedQueries = new HashMap<>();
        predefinedQueries.put(new PredefinedQueryIdentifier("AvgSpeed_49erEuros2015", "49er Euros 2015: Average Speed grouped by Competitor Team Name and Leg Type"),
                              create_AvgSpeed_49erEuros2015_CompetitorName_LegType());
        predefinedQueries.put(new PredefinedQueryIdentifier("AvgSpeed_Per_Regatta-Race", "Average Speed grouped by Regatta Name and Race Name"),
                              create_AvgSpeed_Per_Regatta_Race());
        predefinedQueries.put(new PredefinedQueryIdentifier("AvgSpeed_Per_Competitor-LegType", "Average Speed grouped by Competitor Team Name and Leg Type"),
                              create_AvgSpeed_Per_Competitor_LegType());
        predefinedQueries.put(new PredefinedQueryIdentifier("SumDistance_Per_Competitor-LegType", "Sum of the traveled Distance grouped by Competitor Team Name and Leg Type"),
                              create_SumDistance_Per_Competitor_LegType());
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

        HashMap<FunctionDTO, HashSet<? extends Serializable>> retrieverlevel2_FilterSelection = new HashMap<>();
        FunctionDTO filterDimension0 = new FunctionDTO(true, "getRegatta().getName()", HasTrackedRaceContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
        HashSet<Serializable> filterDimension0_Selection = new HashSet<>();
        filterDimension0_Selection.add("49er Euros 2015");
        retrieverlevel2_FilterSelection.put(filterDimension0, filterDimension0_Selection);
        queryDefinition.setFilterSelectionFor(retrieverChain.getRetrieverLevel(2), retrieverlevel2_FilterSelection);

        FunctionDTO dimensionToGroupBy0 = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getCompetitor().getTeam().getName()", HasGPSFixContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy0);

        FunctionDTO dimensionToGroupBy1 = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getTrackedLegContext().getLegType()", HasGPSFixContext.class.getName(), LegType.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy1);
        
        return queryDefinition;
    }
    
    private StatisticQueryDefinitionDTO create_AvgSpeed_Per_Regatta_Race() {
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

        FunctionDTO dimensionToGroupBy0 = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext().getRegatta().getName()", HasGPSFixContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy0);

        FunctionDTO dimensionToGroupBy1 = new FunctionDTO(true, "getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext().getRace().getName()", HasGPSFixContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy1);
        
        return queryDefinition;
    }
    
    private StatisticQueryDefinitionDTO create_AvgSpeed_Per_Competitor_LegType() {
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
    
    private StatisticQueryDefinitionDTO create_SumDistance_Per_Competitor_LegType() {
        FunctionDTO statistic = new FunctionDTO(false, "getDistanceTraveled()", HasTrackedLegOfCompetitorContext.class.getName(), Distance.class.getName(), new ArrayList<String>(), "", 0);
        AggregationProcessorDefinitionDTO aggregator = new AggregationProcessorDefinitionDTO("Sum", Distance.class.getName(), Distance.class.getName(), "");

        ArrayList<DataRetrieverLevelDTO> retrieverLevels = new ArrayList<>();
        retrieverLevels.add(new DataRetrieverLevelDTO(0, LeaderboardGroupRetrievalProcessor.class.getName(), new LocalizedTypeDTO(LeaderboardGroupWithContext.class.getName(), "Leaderboard Group"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(1, LeaderboardRetrievalProcessor.class.getName(), new LocalizedTypeDTO(HasLeaderboardContext.class.getName(), "Leaderboard"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(2, TrackedRaceRetrievalProcessor.class.getName(), new LocalizedTypeDTO(HasTrackedRaceContext.class.getName(), "Race"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(3, TrackedLegRetrievalProcessor.class.getName(), new LocalizedTypeDTO(HasTrackedLegContext.class.getName(), "Leg"), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(4, TrackedLegOfCompetitorRetrievalProcessor.class.getName(), new LocalizedTypeDTO(HasTrackedLegOfCompetitorContext.class.getName(), "Leg of competitor"), null));
        DataRetrieverChainDefinitionDTO retrieverChain = new DataRetrieverChainDefinitionDTO("", RacingEventService.class.getName(), retrieverLevels);

        ModifiableStatisticQueryDefinitionDTO queryDefinition = new ModifiableStatisticQueryDefinitionDTO("default", statistic, aggregator, retrieverChain);

        FunctionDTO dimensionToGroupBy0 = new FunctionDTO(true, "getCompetitor().getTeam().getName()", HasTrackedLegOfCompetitorContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy0);

        FunctionDTO dimensionToGroupBy1 = new FunctionDTO(true, "getTrackedLegContext().getLegType()", HasTrackedLegOfCompetitorContext.class.getName(), LegType.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy1);
        
        return queryDefinition;
    }

    public Map<PredefinedQueryIdentifier, StatisticQueryDefinitionDTO> get() {
        return predefinedQueries;
    }

}
