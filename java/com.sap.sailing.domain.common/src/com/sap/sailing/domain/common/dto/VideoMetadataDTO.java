package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.Date;

public class VideoMetadataDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean canDownload;
    private Date recordStartedTime;
    private boolean spherical;
    private String message;

    public VideoMetadataDTO() {

    }

    public VideoMetadataDTO(boolean canDownload, boolean spherical, Date recordStartedTime, String message) {
        this.canDownload = canDownload;
        this.spherical = spherical;
        this.recordStartedTime = recordStartedTime;
        this.message = message;
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

    public String getMessage() {
        return message;
    }
}
