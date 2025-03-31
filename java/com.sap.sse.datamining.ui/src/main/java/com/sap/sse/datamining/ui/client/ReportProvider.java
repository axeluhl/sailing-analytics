package com.sap.sse.datamining.ui.client;

import com.sap.sse.datamining.shared.dto.StoredDataMiningReportDTO;

public interface ReportProvider {
    StoredDataMiningReportDTO getCurrentReport();
    void setCurrentReport(StoredDataMiningReportDTO report);
}
