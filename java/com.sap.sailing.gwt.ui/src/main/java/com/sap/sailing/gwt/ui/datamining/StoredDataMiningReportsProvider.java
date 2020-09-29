package com.sap.sailing.gwt.ui.datamining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningReportDTO;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningReportDTOImpl;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataMiningWriteServiceAsync;

public class StoredDataMiningReportsProvider {
    private final Set<StoredDataMiningReportDTO> storedReports = new HashSet<>();
    private final Set<Consumer<Collection<StoredDataMiningReportDTO>>> listeners = new HashSet<>();

    private final DataMiningServiceAsync dataMiningService;
    private final DataMiningWriteServiceAsync dataMiningWriteService;

    public StoredDataMiningReportsProvider(DataMiningServiceAsync dataMiningService, DataMiningWriteServiceAsync dataMiningWriteService) {
        this.dataMiningService = dataMiningService;
        this.dataMiningWriteService = dataMiningWriteService;
        reloadReports();
    }
    
    public void addReportsChangedListener(Consumer<Collection<StoredDataMiningReportDTO>> listener) {
        listeners.add(listener);
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

        dataMiningWriteService.updateOrCreateStoredReport(storedReport, new AsyncCallback<StoredDataMiningReportDTOImpl>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(StoredDataMiningReportDTOImpl result) {
                storedReports.remove(result);
                storedReports.add(result);
                listeners.forEach(l -> l.accept(storedReports));
            }
        });
        return wasUpdate;
    }

    public boolean removeReport(String name) {
        Optional<StoredDataMiningReportDTO> existingStoredReport = findReportByName(name);
        if (!existingStoredReport.isPresent()) {
            return false;
        }

        dataMiningWriteService.removeStoredReport((StoredDataMiningReportDTOImpl) existingStoredReport.get(),
                new AsyncCallback<StoredDataMiningReportDTOImpl>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage(), caught);
                    }

                    @Override
                    public void onSuccess(StoredDataMiningReportDTOImpl result) {
                        storedReports.remove(result);
                        listeners.forEach(l -> l.accept(storedReports));
                    }
                });
        return true;
    }

    /** Reloads the available {@link StoredDataMiningReportDTO stored reports} from the data mining service. */
    public void reloadReports() {
        dataMiningService.retrieveStoredReports(new AsyncCallback<ArrayList<StoredDataMiningReportDTOImpl>>() {
            @Override
            public void onSuccess(ArrayList<StoredDataMiningReportDTOImpl> result) {
                storedReports.clear();
                storedReports.addAll(result);
                listeners.forEach(l -> l.accept(storedReports));
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }
        });
    }

    public Optional<StoredDataMiningReportDTO> findReportByName(final String name) {
        return storedReports.stream().filter(s -> s.getName().equals(name)).findAny();
    }

    /**
     * @return true if <code>text</code> is a valid name of a stored report, false otherwise
     */
    public boolean hasReportWithName(String text) {
        return findReportByName(text).isPresent();
    }
}
