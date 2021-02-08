package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public class RegattaLogSensorDataAddMappingsDialog extends AbstractRegattaLogSensorDataAddMappingsDialog {

    private final String importerType;

    public RegattaLogSensorDataAddMappingsDialog(final SailingServiceWriteAsync sailingServiceWrite, final UserService userService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            Collection<TrackFileImportDeviceIdentifierDTO> importedDeviceIds, String importerType,
            DialogCallback<Collection<TypedDeviceMappingDTO>> callback) {
        super(sailingServiceWrite, userService, errorReporter, stringMessages, leaderboardName, callback);
        this.importerType = importerType;
        deviceIdTable.getDataProvider().getList().addAll(importedDeviceIds);
    }
    
    @Override
    protected String getSelectedImporterType() {
        return importerType;
    }
}
