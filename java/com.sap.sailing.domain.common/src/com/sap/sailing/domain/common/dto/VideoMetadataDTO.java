package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.Date;

import com.sap.sse.common.Duration;

public class VideoMetadataDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean canDownload;
    private Date recordStartedTime;
    private boolean spherical;
    private String message;
    private Duration duration;

    @Deprecated
    VideoMetadataDTO() {} // for GWT serialization only

    public VideoMetadataDTO(boolean canDownload, Duration duration, boolean spherical, Date recordStartedTime, String message) {
        this.canDownload = canDownload;
        this.duration = duration;
        this.spherical = spherical;
        this.recordStartedTime = recordStartedTime;
        this.message = message;
    }

    public boolean isDownloadable() {
        return canDownload;
    }

    public Duration getDuration() {
        return duration;
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
