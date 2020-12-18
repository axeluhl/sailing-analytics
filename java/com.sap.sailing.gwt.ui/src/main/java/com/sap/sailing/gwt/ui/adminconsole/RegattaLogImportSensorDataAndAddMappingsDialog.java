package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public class RegattaLogImportSensorDataAndAddMappingsDialog extends AbstractRegattaLogSensorDataAddMappingsDialog {

    private final SensorDataImportWidget importWidget;

    public RegattaLogImportSensorDataAndAddMappingsDialog(SailingServiceWriteAsync sailingServiceWrite, final UserService userService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            DialogCallback<Collection<TypedDeviceMappingDTO>> callback) {
        super(sailingServiceWrite, userService, errorReporter, stringMessages, leaderboardName, callback);
        this.importWidget = new SensorDataImportWidget(deviceIdTable, stringMessages, sailingServiceWrite, errorReporter);
        setImportWidget(importWidget);
    }
    
    @Override
    protected String getSelectedImporterType() {
        return importWidget.getSelectedImporterType();
    }
}
