package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;

import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningReportDTOImpl;

/** Instances of this class can load, update, create and remove stored data mining reports from the user store. */
public interface StoredDataMiningReportPersister {

    ArrayList<StoredDataMiningReportDTOImpl> getStoredReports();

    StoredDataMiningReportDTOImpl updateOrCreateStoredReport(StoredDataMiningReportDTOImpl report);

    StoredDataMiningReportDTOImpl removeStoredReport(StoredDataMiningReportDTOImpl report);

}