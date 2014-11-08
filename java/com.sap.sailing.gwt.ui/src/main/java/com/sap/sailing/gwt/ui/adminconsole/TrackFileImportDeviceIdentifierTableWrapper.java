package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class TrackFileImportDeviceIdentifierTableWrapper extends TableWrapper<TrackFileImportDeviceIdentifierDTO,
SingleSelectionModel<TrackFileImportDeviceIdentifierDTO>> {
    
    public TrackFileImportDeviceIdentifierTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, new SingleSelectionModel<TrackFileImportDeviceIdentifierDTO>(), true);
        
        TextColumn<TrackFileImportDeviceIdentifierDTO> uuidColumn = new TextColumn<TrackFileImportDeviceIdentifierDTO>() {
            @Override
            public String getValue(TrackFileImportDeviceIdentifierDTO object) {
                return object.uuidAsString;
            }
        };
        
        TextColumn<TrackFileImportDeviceIdentifierDTO> fileNameColumn = new TextColumn<TrackFileImportDeviceIdentifierDTO>() {
            @Override
            public String getValue(TrackFileImportDeviceIdentifierDTO object) {
                return object.fileName;
            }
        };
        
        TextColumn<TrackFileImportDeviceIdentifierDTO> trackNameColumn = new TextColumn<TrackFileImportDeviceIdentifierDTO>() {
            @Override
            public String getValue(TrackFileImportDeviceIdentifierDTO object) {
                return object.trackName;
            }
        };
        
        TextColumn<TrackFileImportDeviceIdentifierDTO> fromColumn = new TextColumn<TrackFileImportDeviceIdentifierDTO>() {
            @Override
            public String getValue(TrackFileImportDeviceIdentifierDTO object) {
                return DateAndTimeFormatterUtil.formatDateAndTime(object.from);
            }
        };
        
        TextColumn<TrackFileImportDeviceIdentifierDTO> toColumn = new TextColumn<TrackFileImportDeviceIdentifierDTO>() {
            @Override
            public String getValue(TrackFileImportDeviceIdentifierDTO object) {
                return DateAndTimeFormatterUtil.formatDateAndTime(object.to);
            }
        };
        
        TextColumn<TrackFileImportDeviceIdentifierDTO> numberOfFixesColumn = new TextColumn<TrackFileImportDeviceIdentifierDTO>() {
            @Override
            public String getValue(TrackFileImportDeviceIdentifierDTO object) {
                return "" + object.numFixes;
            }
        };

        table.addColumn(uuidColumn, "UUID");
        table.addColumn(fileNameColumn, "Filename");
        table.addColumn(trackNameColumn, "Trackname");
        table.addColumn(fromColumn, stringMessages.from());
        table.addColumn(toColumn, stringMessages.to());
        table.addColumn(numberOfFixesColumn, "# fixes");
    }

}
