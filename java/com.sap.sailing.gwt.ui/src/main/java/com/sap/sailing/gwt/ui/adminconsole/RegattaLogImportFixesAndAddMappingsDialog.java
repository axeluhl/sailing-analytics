package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public class RegattaLogImportFixesAndAddMappingsDialog extends AbstractRegattaLogFixesAddMappingsDialog {
    TrackFileImportWidget importWidget;

    public RegattaLogImportFixesAndAddMappingsDialog(SailingServiceWriteAsync sailingServiceWrite, final UserService userService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            DialogCallback<Collection<DeviceMappingDTO>> callback) {
        super(sailingServiceWrite, userService, errorReporter, stringMessages, leaderboardName, callback);
        importWidget = new TrackFileImportWidget(deviceIdTable, stringMessages, sailingServiceWrite, errorReporter);
        setImportWidget(importWidget);
    }
}
