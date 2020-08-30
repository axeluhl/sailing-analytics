package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;

public interface DataMiningReportDTO extends Serializable {
    
    ArrayList<StatisticQueryDefinitionDTO> getQueryDefinitions();
    
    // TODO Add parameters

}
