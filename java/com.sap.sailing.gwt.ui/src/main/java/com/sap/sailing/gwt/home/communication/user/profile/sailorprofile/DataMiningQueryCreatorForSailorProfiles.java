package com.sap.sailing.gwt.home.communication.user.profile.sailorprofile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Distance;
import com.sap.sse.datamining.shared.DataMiningQuerySerializer;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;

public final class DataMiningQueryCreatorForSailorProfiles {
    private DataMiningQueryCreatorForSailorProfiles() {
    }

    private static final String leaderboardGroupRetrievalProcessorName = "com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor";
    private static final String leaderboardRetrievalProcessorName = "com.sap.sailing.datamining.impl.components.LeaderboardRetrievalProcessor";
    private static final String trackedRaceRetrievalProcessorName = "com.sap.sailing.datamining.impl.components.TrackedRaceRetrievalProcessor";
    private static final String raceCompetitorRetrievalProcessorName = "com.sap.sailing.datamining.impl.components.RaceOfCompetitorRetrievalProcessor";

    public static final String getSerializedDataMiningQuery(SailorProfileNumericStatisticType type,
            List<String> competitorNames) {
        switch (type) {
        case AVERAGE_STARTLINE_DISTANCE:
            return DataMiningQuerySerializer.toBase64String(createQueryForAverageStartlineDistance(competitorNames));
        default:
            return null;
        }
    }

    private static StatisticQueryDefinitionDTO createQueryForAverageStartlineDistance(
            final List<String> competitorNames) {
        FunctionDTO statistic = new FunctionDTO(false, "getDistanceToStartLineAtStart()",
                HasRaceOfCompetitorContext.class.getName(), Distance.class.getName(), new ArrayList<String>(), "", 0);
        AggregationProcessorDefinitionDTO aggregator = new AggregationProcessorDefinitionDTO("Minimum",
                Distance.class.getName(), Distance.class.getName(), "");

        ArrayList<DataRetrieverLevelDTO> retrieverLevels = new ArrayList<>();
        retrieverLevels.add(new DataRetrieverLevelDTO(0, leaderboardGroupRetrievalProcessorName,
                new LocalizedTypeDTO(HasLeaderboardGroupContext.class.getName(), ""), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(1, leaderboardRetrievalProcessorName,
                new LocalizedTypeDTO(HasLeaderboardContext.class.getName(), ""), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(2, trackedRaceRetrievalProcessorName,
                new LocalizedTypeDTO(HasTrackedRaceContext.class.getName(), ""), null));
        retrieverLevels.add(new DataRetrieverLevelDTO(3, raceCompetitorRetrievalProcessorName,
                new LocalizedTypeDTO(HasRaceOfCompetitorContext.class.getName(), ""), null));
        DataRetrieverChainDefinitionDTO retrieverChain = new DataRetrieverChainDefinitionDTO("",
                RacingEventService.class.getName(), retrieverLevels);

        ModifiableStatisticQueryDefinitionDTO queryDefinition = new ModifiableStatisticQueryDefinitionDTO("en",
                statistic, aggregator, retrieverChain);

        HashMap<FunctionDTO, HashSet<? extends Serializable>> retrieverlevel3_FilterSelection = new HashMap<>();
        FunctionDTO filterDimension0 = new FunctionDTO(true, "getCompetitor().getName()",
                HasRaceOfCompetitorContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
        HashSet<Serializable> filterDimension0_Selection = new HashSet<>();
        competitorNames.forEach(filterDimension0_Selection::add);
        retrieverlevel3_FilterSelection.put(filterDimension0, filterDimension0_Selection);
        queryDefinition.setFilterSelectionFor(retrieverChain.getRetrieverLevel(3), retrieverlevel3_FilterSelection);

        FunctionDTO dimensionToGroupBy0 = new FunctionDTO(true, "getTrackedRaceContext().getRace().getName()",
                HasRaceOfCompetitorContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy0);

        FunctionDTO dimensionToGroupBy1 = new FunctionDTO(true, "getCompetitor().getName()",
                HasRaceOfCompetitorContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
        queryDefinition.appendDimensionToGroupBy(dimensionToGroupBy1);

        return queryDefinition;
    }

}
