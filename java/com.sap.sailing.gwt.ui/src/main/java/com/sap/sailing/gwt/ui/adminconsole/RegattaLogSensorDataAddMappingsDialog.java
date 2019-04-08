package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLogSensorDataAddMappingsDialog extends AbstractRegattaLogSensorDataAddMappingsDialog {

    private final String importerType;

    public RegattaLogSensorDataAddMappingsDialog(SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            Collection<TrackFileImportDeviceIdentifierDTO> importedDeviceIds, String importerType,
            DialogCallback<Collection<TypedDeviceMappingDTO>> callback) {
        super(sailingService, errorReporter, stringMessages, leaderboardName, callback);
        this.importerType = importerType;
        deviceIdTable.getDataProvider().getList().addAll(importedDeviceIds);
    }
    
    @Override
    protected String getSelectedImporterType() {
        return importerType;
    }
}
