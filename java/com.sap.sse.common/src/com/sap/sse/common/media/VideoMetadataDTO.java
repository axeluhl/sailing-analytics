package com.sap.sse.common.media;

import java.util.Date;

public class VideoMetadataDTO extends AbstractMediaDTO {
    
    private static final long serialVersionUID = 1L;

    /**
     * TODO: Do we really need video length? Should it be mandatory?
     */
    private int lengthInSeconds;

    /**
     * URL Reference to thumbnail image. This information works as override for youtube videos or as missing thumbnail
     * information for other formats. It can be either a link to thumbnail or even be a data/url contaning the base64
     * encoded image.
     */
    private String thumbnailRef;


    protected VideoMetadataDTO() {
    }

    
    public VideoMetadataDTO(String url, MimeType mimeType, Date createdAtDate) {
        super(url, mimeType, createdAtDate);
    }

    public int getLengthInSeconds() {
        return lengthInSeconds;
    }

    public void setLengthInSeconds(int lengthInSeconds) {
        this.lengthInSeconds = lengthInSeconds;
    }

    public void setThumbnailRef(String thumbnailRef) {
        this.thumbnailRef = thumbnailRef;
    }

    public String getThumbnailRef() {
        return thumbnailRef;
    }
}
