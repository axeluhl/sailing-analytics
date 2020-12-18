package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public class RegattaLogFixesAddMappingsDialog extends AbstractRegattaLogFixesAddMappingsDialog {
    TrackFileImportWidget importWidget;

    public RegattaLogFixesAddMappingsDialog(SailingServiceWriteAsync sailingServiceWrite, final UserService userService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            Collection<TrackFileImportDeviceIdentifierDTO> importedDeviceIds,
            DialogCallback<Collection<DeviceMappingDTO>> callback) {
        super(sailingServiceWrite, userService, errorReporter, stringMessages, leaderboardName, callback);
        deviceIdTable.getDataProvider().getList().addAll(importedDeviceIds);
    }
}
