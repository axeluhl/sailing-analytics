package com.sap.sse.datamining.shared.dto;

import java.util.UUID;

import com.sap.sse.common.Named;

public interface StoredDataMiningReportDTO extends Named {
    UUID getId();
    DataMiningReportDTO getReport();
}
