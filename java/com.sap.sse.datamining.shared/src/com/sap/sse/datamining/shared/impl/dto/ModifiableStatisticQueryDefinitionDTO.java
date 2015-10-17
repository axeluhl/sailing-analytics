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

    @Override
    public String getLocaleInfoName() {
        return localeInfoName;
    }
    
    public void setLocaleInfoName(String localeInfoName) {
        if (localeInfoName == null) {
            throw new NullPointerException("The locale info name mustn't be null");
        }
        this.localeInfoName = localeInfoName;
    }
    
    @Override
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition() {
        return dataRetrieverChainDefinition;
    }
    
    public void setDataRetrieverChainDefinition(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition) {
        if (dataRetrieverChainDefinition == null) {
            throw new NullPointerException("The data retriever chain definition mustn't be null");
        }
        this.dataRetrieverChainDefinition = dataRetrieverChainDefinition;
    }
    
    @Override
    public HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings() {
        return retrieverSettings;
    }
    
    public void setRetrieverSettings(DataRetrieverLevelDTO retrieverLevel, SerializableSettings settings) {
        if (retrieverLevel == null) {
            throw new NullPointerException("The retriever level mustn't be null");
        }
        retrieverSettings.put(retrieverLevel, settings);
    }

    @Override
    public HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> getFilterSelection() {
        return filterSelection;
    }
    
    public void setFilterSelectionFor(DataRetrieverLevelDTO retrieverLevel, HashMap<FunctionDTO, HashSet<? extends Serializable>> levelFilterSelection) {
        if (retrieverLevel == null) {
            throw new NullPointerException("The retriever level mustn't be null");
        }
        if (levelFilterSelection == null) {
            throw new NullPointerException("The level filter selection mustn't be null");
        }
        
        filterSelection.put(retrieverLevel, levelFilterSelection);
    }

    @Override
    public ArrayList<FunctionDTO> getDimensionsToGroupBy() {
        return dimensionsToGroupBy;
    }
    
    public void appendDimensionToGroupBy(FunctionDTO dimensionToGroupBy) {
        if (dimensionToGroupBy == null) {
            throw new NullPointerException("The dimension mustn't be null");
        }
        dimensionsToGroupBy.add(dimensionToGroupBy);
    }

    @Override
    public FunctionDTO getStatisticToCalculate() {
        return statisticToCalculate;
    }
    
    public void setStatisticToCalculate(FunctionDTO statisticToCalculate) {
        if (statisticToCalculate == null) {
            throw new NullPointerException("The statistic to calculate mustn't be null");
        }
        this.statisticToCalculate = statisticToCalculate;
    }

    @Override
    public AggregationProcessorDefinitionDTO getAggregatorDefinition() {
        return aggregatorDefinition;
    }
    
    public void setAggregatorDefinition(AggregationProcessorDefinitionDTO aggregatorDefinition) {
        if (aggregatorDefinition == null) {
            throw new NullPointerException("The aggregator definition mustn't be null");
        }
        this.aggregatorDefinition = aggregatorDefinition;
    }

}
