package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.UUID;

public interface StoredDataMiningReportDTO extends Serializable {

    String getName();
    UUID getId();
    DataMiningReportDTO getReport();

}
