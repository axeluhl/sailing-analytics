package com.sap.sse.datamining.impl.components.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.sap.sse.datamining.components.management.QueryDefinitionDTOProvider;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;

public class QueryDefinitionDTOManager implements QueryDefinitionDTOProvider {

    private final Map<String, StatisticQueryDefinitionDTO> definitionDTOsMappedByName;
    
    public QueryDefinitionDTOManager() {
        definitionDTOsMappedByName = new HashMap<>();

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
        
        definitionDTOsMappedByName.put("49er Euros 2015: Average Speed grouped by Competitor and Leg Type", avgSpeed_49erEuros2015_CompetitorName_LegType);
    }

    @Override
    public Iterable<String> getNames() {
        return definitionDTOsMappedByName.keySet();
    }

    @Override
    public StatisticQueryDefinitionDTO get(String name) {
        return definitionDTOsMappedByName.get(name);
    }

}
