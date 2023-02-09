package com.sap.sse.datamining.ui.client;

import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;

public interface ReportProvider {
    DataMiningReportDTO getCurrentReport();
    void setCurrentReport(DataMiningReportDTO report);
}
