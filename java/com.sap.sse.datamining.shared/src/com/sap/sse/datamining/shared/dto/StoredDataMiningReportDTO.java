package com.sap.sse.datamining.shared.dto;

import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.common.Renamable;

public interface StoredDataMiningReportDTO extends NamedWithUUID, Renamable {
    DataMiningReportDTO getReport();
}
