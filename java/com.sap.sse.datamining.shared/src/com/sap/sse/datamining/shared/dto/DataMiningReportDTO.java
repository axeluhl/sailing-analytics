package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;

public interface DataMiningReportDTO extends Serializable {
    
    Iterable<StatisticQueryDefinitionDTO> getQueryDefinitions();
    
    // TODO Add parameters

}
