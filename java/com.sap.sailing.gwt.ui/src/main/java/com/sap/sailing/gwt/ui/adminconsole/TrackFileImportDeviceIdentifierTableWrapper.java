package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.adminconsole.TrackFileImportDeviceIdentifierTableWrapper.TrackFileImportDeviceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TrackFileImportDeviceIdentifierTableWrapper extends TableWrapper<TrackFileImportDeviceIdentifier, SingleSelectionModel<TrackFileImportDeviceIdentifier>> {
    public static class TrackFileImportDeviceIdentifier {
        public String uuid;
        public String fileName;
        public String trackName;
        public Date from;
        public Date to;
        public long numberOfFixes;
        
        public TrackFileImportDeviceIdentifier(String uuid, String fileName, String trackName, Date from, Date to, long numberOfFixes) {
            this.fileName = fileName;
            this.trackName = trackName;
            this.from = from;
            this.to = to;
            this.numberOfFixes = numberOfFixes;
            this.uuid = uuid;
        }
    }
    
    public TrackFileImportDeviceIdentifierTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, new SingleSelectionModel<TrackFileImportDeviceIdentifier>());
        
        TextColumn<TrackFileImportDeviceIdentifier> fileNameColumn = new TextColumn<TrackFileImportDeviceIdentifier>() {
            @Override
            public String getValue(TrackFileImportDeviceIdentifier object) {
                return object.fileName;
            }
        };
        
        TextColumn<TrackFileImportDeviceIdentifier> trackNameColumn = new TextColumn<TrackFileImportDeviceIdentifier>() {
            @Override
            public String getValue(TrackFileImportDeviceIdentifier object) {
                return object.trackName;
            }
        };
        
//        TextColumn<TrackFileImportDeviceIdentifier> fromColumn = new TextColumn<TrackFileImportDeviceIdentifier>() {
//            @Override
//            public String getValue(TrackFileImportDeviceIdentifier object) {
//                return DateAndTimeFormatterUtil.formatDateAndTime(object.from);
//            }
//        };
//        
//        TextColumn<TrackFileImportDeviceIdentifier> toColumn = new TextColumn<TrackFileImportDeviceIdentifier>() {
//            @Override
//            public String getValue(TrackFileImportDeviceIdentifier object) {
//                return DateAndTimeFormatterUtil.formatDateAndTime(object.to);
//            }
//        };
//        
//        TextColumn<TrackFileImportDeviceIdentifier> numberOfFixesColumn = new TextColumn<TrackFileImportDeviceIdentifier>() {
//            @Override
//            public String getValue(TrackFileImportDeviceIdentifier object) {
//                return "" + object.numberOfFixes;
//            }
//        };
        
        table.addColumn(fileNameColumn, "Filename");
        table.addColumn(trackNameColumn, "Trackname");
//        table.addColumn(fromColumn, stringMessages.from());
//        table.addColumn(toColumn, stringMessages.to());
//        table.addColumn(numberOfFixesColumn, "# fixes");
    }

}
