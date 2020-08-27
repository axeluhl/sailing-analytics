package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.shiro.authz.AuthorizationException;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.impl.preferences.model.SailingPreferences;
import com.sap.sailing.server.impl.preferences.model.StoredDataMiningQueryPreference;
import com.sap.sailing.server.impl.preferences.model.StoredDataMiningReportPreference;
import com.sap.sailing.server.impl.preferences.model.StoredDataMiningReportPreferences;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.shared.DataMiningReportSerializer;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningReportDTOImpl;
import com.sap.sse.gwt.dispatch.shared.exceptions.ServerDispatchException;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.util.JoinedClassLoader;

public class StoredDataMiningReportPersisterImpl implements StoredDataMiningReportPersister {

    private final SecurityService securityService;
    private final ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker;

    public StoredDataMiningReportPersisterImpl(SecurityService securityService,
            ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker) {
        this.securityService = securityService;
        this.dataMiningServerTracker = dataMiningServerTracker;
    }

    @Override
    public ArrayList<StoredDataMiningReportDTOImpl> getStoredReports() {
        StoredDataMiningReportPreferences reportPreferences = getUserPreferences();
        if (reportPreferences == null) {
            return new ArrayList<>();
        }

        Stream<StoredDataMiningReportPreference> preferenceStream = StreamSupport
                .stream(reportPreferences.getStoredReports().spliterator(), false);
        return new ArrayList<>(preferenceStream.map(this::transform).collect(Collectors.toList()));
    }

    @Override
    public StoredDataMiningReportDTOImpl updateOrCreateStoredReport(StoredDataMiningReportDTOImpl report) {
        StoredDataMiningReportPreferences reportPreferences = getUserPreferences();

        Collection<StoredDataMiningReportPreference> updatedReports = filterReport(report, reportPreferences);
        updatedReports.add(transform(report));

        StoredDataMiningReportPreferences updatedPreferences = new StoredDataMiningReportPreferences();
        updatedPreferences.setStoredReports(updatedReports);
        setUserPreferences(updatedPreferences);
        return report;
    }

    @Override
    public StoredDataMiningReportDTOImpl removeStoredReport(StoredDataMiningReportDTOImpl report) {
        StoredDataMiningReportPreferences reportPreferences = getUserPreferences();

        Collection<StoredDataMiningReportPreference> updatedReports = filterReport(report, reportPreferences);

        StoredDataMiningReportPreferences updatedPreferences = new StoredDataMiningReportPreferences();
        updatedPreferences.setStoredReports(updatedReports);
        setUserPreferences(updatedPreferences);
        return report;
    }

    private Collection<StoredDataMiningReportPreference> filterReport(StoredDataMiningReportDTOImpl report,
            StoredDataMiningReportPreferences prefs) {
        if (prefs == null) {
            return new ArrayList<>();
        }

        Stream<StoredDataMiningReportPreference> preferenceStream = StreamSupport
                .stream(prefs.getStoredReports().spliterator(), false);
        Collection<StoredDataMiningReportPreference> updatedEntries = new ArrayList<>(
                preferenceStream.filter(r -> !r.getId().equals(report.getId())).collect(Collectors.toList()));
        return updatedEntries;
    }

    private void setUserPreferences(StoredDataMiningReportPreferences preferences) {
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null) {
            try {
                securityService.setPreferenceObject(currentUser.getName(),
                        SailingPreferences.STORED_DATAMINING_REPORT_PREFERENCES, preferences);
            } catch (AuthorizationException e) {
                throw new ServerDispatchException(e);
            }
        }
    }

    /** Converts a {@link StoredDataMiningQueryPreference} to a {@link StoredDataMiningQueryDTO}. */
    private StoredDataMiningReportDTOImpl transform(StoredDataMiningReportPreference pref) {
        JoinedClassLoader joinedClassLoader = dataMiningServerTracker.getService().getJoinedClassLoader();
        DataMiningReportDTO report = DataMiningReportSerializer.reportFromBase64(pref.getSerializedReport(), joinedClassLoader);
        return new StoredDataMiningReportDTOImpl(pref.getId(), pref.getName(), report);
    }

    /** Converts a {@link StoredDataMiningQueryDTO} to a {@link StoredDataMiningQueryPreference}. */
    private StoredDataMiningReportPreference transform(StoredDataMiningReportDTOImpl dto) {
        String serializedQuery = DataMiningReportSerializer.reportToBase64(dto.getReport());
        return new StoredDataMiningReportPreference(dto.getName(), dto.getId(), serializedQuery);
    }

    private StoredDataMiningReportPreferences getUserPreferences() {
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null) {
            return securityService.getPreferenceObject(currentUser.getName(),
                    SailingPreferences.STORED_DATAMINING_REPORT_PREFERENCES);
        }
        return null;
    }
}
