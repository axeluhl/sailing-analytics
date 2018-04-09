package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLogImportSensorDataAndAddMappingsDialog extends AbstractRegattaLogSensorDataAddMappingsDialog {

    private final SensorDataImportWidget importWidget;

    public RegattaLogImportSensorDataAndAddMappingsDialog(SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            DialogCallback<Collection<TypedDeviceMappingDTO>> callback) {
        super(sailingService, errorReporter, stringMessages, leaderboardName, callback);
        this.importWidget = new SensorDataImportWidget(deviceIdTable, stringMessages, sailingService, errorReporter);
        setImportWidget(importWidget);
    }
    
    @Override
    protected String getSelectedImporterType() {
        return importWidget.getSelectedImporterType();
    }
}
