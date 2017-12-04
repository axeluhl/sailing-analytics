package com.sap.sailing.domain.common.dto;

import java.util.Date;

public class VideoMetadataDTO {

    private boolean canDownload;
    private Date recordStartedTime;
    private boolean spherical;

    public VideoMetadataDTO(boolean canDownload, boolean spherical, Date recordStartedTime) {
        this.canDownload = canDownload;
        this.spherical = spherical;
        this.recordStartedTime = recordStartedTime;
    }
    
    public boolean isDownloadable() {
        return canDownload;
    }
    
    public boolean isSpherical() {
        return spherical;
    }
    
    public Date getRecordStartedTime() {
        return recordStartedTime;
    }
}
