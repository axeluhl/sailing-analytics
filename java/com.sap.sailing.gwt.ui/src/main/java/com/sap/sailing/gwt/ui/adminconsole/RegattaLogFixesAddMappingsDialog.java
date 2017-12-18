package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLogFixesAddMappingsDialog extends AbstractRegattaLogFixesAddMappingsDialog {
    TrackFileImportWidget importWidget;

    public RegattaLogFixesAddMappingsDialog(SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            Collection<TrackFileImportDeviceIdentifierDTO> importedDeviceIds,
            DialogCallback<Collection<DeviceMappingDTO>> callback) {
        super(sailingService, errorReporter, stringMessages, leaderboardName, callback);
        deviceIdTable.getDataProvider().getList().addAll(importedDeviceIds);
    }
}
