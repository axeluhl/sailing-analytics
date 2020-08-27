package com.sap.sailing.gwt.ui.datamining.reports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.datamining.StoredDataMiningQueryPanel;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningReportDTO;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningReportDTOImpl;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;

public class StoredDataMiningReportsProvider {
    private final Set<StoredDataMiningReportDTO> storedReports = new HashSet<>();

    private final DataMiningServiceAsync dataMiningService;

    public StoredDataMiningReportsProvider(DataMiningServiceAsync dataMiningService) {
        this.dataMiningService = dataMiningService;
    }

    /**
     * Creates a new stored report with the {@link name} or updates an existing one.
     * 
     * @return true, if stored report was present and this is an update<br/>
     *         false, if a new stored report was created
     */
    public boolean addOrUpdateReport(String name, DataMiningReportDTO report) {
        Optional<StoredDataMiningReportDTO> existingStoredReport = findReportByName(name);

        boolean wasUpdate = !existingStoredReport.isPresent();
        StoredDataMiningReportDTOImpl storedReport;
        if (existingStoredReport.isPresent()) {
            storedReport = new StoredDataMiningReportDTOImpl(existingStoredReport.get().getId(), name, report);
        } else {
            storedReport = new StoredDataMiningReportDTOImpl(UUID.randomUUID(), name, report);
        }

        dataMiningService.updateOrCreateStoredReport(storedReport, new AsyncCallback<StoredDataMiningReportDTOImpl>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(StoredDataMiningReportDTOImpl result) {
                storedReports.remove(result);
                storedReports.add(result);
            }
        });
        return wasUpdate;
    }

    public boolean removeQuery(String name) {
        Optional<StoredDataMiningReportDTO> existingStoredReport = findReportByName(name);
        if (!existingStoredReport.isPresent()) {
            return false;
        }

        dataMiningService.removeStoredReport((StoredDataMiningReportDTOImpl) existingStoredReport.get(),
                new AsyncCallback<StoredDataMiningReportDTOImpl>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage(), caught);
                    }

                    @Override
                    public void onSuccess(StoredDataMiningReportDTOImpl result) {
                        storedReports.remove(result);
                    }
                });
        return true;
    }

    /** Refresh the stored queries and update the {@link StoredDataMiningQueryPanel}. */
    private void refreshQueries() {
        dataMiningService.retrieveStoredReports(new AsyncCallback<ArrayList<StoredDataMiningReportDTOImpl>>() {
            @Override
            public void onSuccess(ArrayList<StoredDataMiningReportDTOImpl> result) {
                storedReports.clear();
                storedReports.addAll(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }
        });
    }

    private Optional<StoredDataMiningReportDTO> findReportByName(final String name) {
        return storedReports.stream().filter(s -> s.getName().equals(name)).findAny();
    }

    /**
     * @return true if <code>text</code> is a valid name of a stored report, false otherwise
     */
    public boolean containsQueryName(String text) {
        return findReportByName(text).isPresent();
    }
}
