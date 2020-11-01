package com.sap.sse.datamining.shared.impl.dto;

import java.util.ArrayList;

import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.DataMiningReportParametersDTO;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.parameters.ModifiableDataMiningReportParametersDTO;

public class ModifiableDataMiningReportDTO implements DataMiningReportDTO {
    private static final long serialVersionUID = -6512175470789118223L;
    
    private ArrayList<StatisticQueryDefinitionDTO> queryDefinitions;
    private DataMiningReportParametersDTO parameters;

    public ModifiableDataMiningReportDTO() {
        this(new ArrayList<>(), new ModifiableDataMiningReportParametersDTO());
    }
    
    public ModifiableDataMiningReportDTO(ArrayList<StatisticQueryDefinitionDTO> queryDefinitions, ModifiableDataMiningReportParametersDTO parameters) {
        this.queryDefinitions = queryDefinitions;
        this.parameters = parameters;
    }

    @Override
    public ArrayList<StatisticQueryDefinitionDTO> getQueryDefinitions() {
        return queryDefinitions;
    }
    
    public void addQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        queryDefinitions.add(queryDefinition);
    }
    
    public boolean removeQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        return queryDefinitions.remove(queryDefinition);
    }

    @Override
    public DataMiningReportParametersDTO getParameters() {
        return parameters;
    }

}
