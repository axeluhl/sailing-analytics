package com.sap.sse.datamining.shared.impl.dto;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public class ModifiableDataMiningReportDTO implements DataMiningReportDTO {
    private static final long serialVersionUID = -6512175470789118223L;
    
    private final List<StatisticQueryDefinitionDTO> queryDefinitions;

    public ModifiableDataMiningReportDTO() {
        this(new ArrayList<>());
    }

    public ModifiableDataMiningReportDTO(List<StatisticQueryDefinitionDTO> queryDefinitions) {
        this.queryDefinitions = queryDefinitions;
    }

    @Override
    public Iterable<StatisticQueryDefinitionDTO> getQueryDefinitions() {
        return this.queryDefinitions;
    }
    
    public void addQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        this.queryDefinitions.add(queryDefinition);
    }
    
    public boolean removeQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        return this.queryDefinitions.remove(queryDefinition);
    }

}
