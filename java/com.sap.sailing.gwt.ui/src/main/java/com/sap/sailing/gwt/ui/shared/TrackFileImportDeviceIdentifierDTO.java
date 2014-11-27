package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;

public class TrackFileImportDeviceIdentifierDTO extends DeviceIdentifierDTO {
    public String uuidAsString;
    public String fileName;
    public String trackName;
    public long numFixes;
    public Date from;
    public Date to;
    
    protected TrackFileImportDeviceIdentifierDTO() {}
    
    public TrackFileImportDeviceIdentifierDTO(String uuidAsString, String fileName, String trackName,
            long numFixes, Date from, Date to) {
        super(TrackFileImportDeviceIdentifier.TYPE, uuidAsString);
        this.uuidAsString = uuidAsString;
        this.fileName = fileName;
        this.trackName = trackName;
        this.numFixes = numFixes;
        this.from = from;
        this.to = to;
    }
}
