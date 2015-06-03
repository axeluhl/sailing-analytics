package com.sap.sse.gwt.client.media;

import java.util.Date;

import com.sap.sse.common.media.MimeType;

public class VideoDTO extends AbstractMediaDTO {
    private int lengthInSeconds;
    private String thumbnailRef;
    
    /** for GWT */
    protected VideoDTO() {
    }

    public VideoDTO(String videoRef, MimeType mimeType, Date createdAtDate) {
        super(videoRef, mimeType, createdAtDate);
    }

    public int getLengthInSeconds() {
        return lengthInSeconds;
    }

    public void setLengthInSeconds(int lengthInSeconds) {
        this.lengthInSeconds = lengthInSeconds;
    }

    public String getThumbnailRef() {
        return thumbnailRef;
    }

    public void setThumbnailRef(String thumbnailRef) {
        this.thumbnailRef = thumbnailRef;
    }
}
