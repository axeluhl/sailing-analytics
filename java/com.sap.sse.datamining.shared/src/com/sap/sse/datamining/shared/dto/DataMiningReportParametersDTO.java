package com.sap.sse.datamining.shared.dto;

import java.util.Map;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface DataMiningReportParametersDTO {
    
    public static class ParameterKey extends Pair<DataRetrieverLevelDTO, FunctionDTO> {
        private static final long serialVersionUID = -6819556797537594552L;

        public ParameterKey(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension) {
            super(retrieverLevel, dimension);
        }
    }
    
    public static class QueryKey extends Pair<FunctionDTO, AggregationProcessorDefinitionDTO> {
        private static final long serialVersionUID = -4239175922626327780L;

        public QueryKey(FunctionDTO statisticToCalculate, AggregationProcessorDefinitionDTO aggregatorDefinition) {
            super(statisticToCalculate, aggregatorDefinition);
        }
    }
    
    Map<ParameterKey, FilterDimensionParameter> getAll();
    boolean contains(ParameterKey key);
    FilterDimensionParameter get(ParameterKey key);
    
}
