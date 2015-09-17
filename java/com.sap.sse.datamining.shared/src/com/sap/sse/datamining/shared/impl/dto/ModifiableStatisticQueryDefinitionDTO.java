package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public class ModifiableStatisticQueryDefinitionDTO implements StatisticQueryDefinitionDTO {
    private static final long serialVersionUID = -6438771277564908352L;
    
    private String localeInfoName;
    private FunctionDTO statisticToCalculate;
    private AggregationProcessorDefinitionDTO aggregatorDefinition;
    private ArrayList<FunctionDTO> dimensionsToGroupBy;
    private DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition;
    private HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettings;
    private HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelection;
    
    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    ModifiableStatisticQueryDefinitionDTO() { }

    public ModifiableStatisticQueryDefinitionDTO(String localeInfoName, FunctionDTO statisticToCalculate, AggregationProcessorDefinitionDTO aggregatorDefinition, DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition) {
        this.localeInfoName = localeInfoName;
        this.statisticToCalculate = statisticToCalculate;
        this.aggregatorDefinition = aggregatorDefinition;
        this.dataRetrieverChainDefinition = dataRetrieverChainDefinition;
        this.retrieverSettings = new HashMap<>();
        this.filterSelection = new HashMap<>();
        this.dimensionsToGroupBy = new ArrayList<FunctionDTO>();
    }
    
    public void setRetrieverSettings(DataRetrieverLevelDTO retrieverLevel, SerializableSettings settings) {
        retrieverSettings.put(retrieverLevel, settings);
    }
    
    public void setFilterSelectionFor(DataRetrieverLevelDTO retrieverLevel, HashMap<FunctionDTO, HashSet<? extends Serializable>> levelFilterSelection) {
        filterSelection.put(retrieverLevel, levelFilterSelection);
    }
    
    public void appendDimensionToGroupBy(FunctionDTO dimensionToGroupBy) {
        dimensionsToGroupBy.add(dimensionToGroupBy);
    }

    @Override
    public String getLocaleInfoName() {
        return localeInfoName;
    }
    
    @Override
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition() {
        return dataRetrieverChainDefinition;
    }
    
    @Override
    public HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings() {
        return retrieverSettings;
    }

    @Override
    public HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> getFilterSelection() {
        return filterSelection;
    }

    @Override
    public ArrayList<FunctionDTO> getDimensionsToGroupBy() {
        return dimensionsToGroupBy;
    }

    @Override
    public FunctionDTO getStatisticToCalculate() {
        return statisticToCalculate;
    }

    @Override
    public AggregationProcessorDefinitionDTO getAggregatorDefinition() {
        return aggregatorDefinition;
    }

}
