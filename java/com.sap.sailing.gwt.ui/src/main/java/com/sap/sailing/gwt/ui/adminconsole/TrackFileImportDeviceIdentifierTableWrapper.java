package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;

public class TrackFileImportDeviceIdentifierTableWrapper extends TableWrapper<TrackFileImportDeviceIdentifierDTO,
RefreshableSingleSelectionModel<TrackFileImportDeviceIdentifierDTO>> {
    
    public TrackFileImportDeviceIdentifierTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, /* multiSelection */ false, /* enablePager */ true,
                new EntityIdentityComparator<TrackFileImportDeviceIdentifierDTO>() {

                    @Override
                    public boolean representSameEntity(TrackFileImportDeviceIdentifierDTO dto1,
                            TrackFileImportDeviceIdentifierDTO dto2) {
                        return dto1.uuidAsString.equals(dto2.uuidAsString);
                    }
                });
        
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
