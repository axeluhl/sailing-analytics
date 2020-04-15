package com.sap.sse.gwt.client.media;

import java.util.Date;

import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;

public class VideoDTO extends AbstractMediaDTO {
    private Integer lengthInSeconds;
    private String thumbnailRef;
    
    /** for GWT */
    @Deprecated
    protected VideoDTO() {
    }

    public VideoDTO(String videoRef, MimeType mimeType, Date createdAtDate) {
        super(videoRef, mimeType, createdAtDate);
    }

    public Integer getLengthInSeconds() {
        return lengthInSeconds;
    }

    public void setLengthInSeconds(Integer lengthInSeconds) {
        this.lengthInSeconds = lengthInSeconds;
    }

    public String getThumbnailRef() {
        return thumbnailRef;
    }

    public void setThumbnailRef(String thumbnailRef) {
        this.thumbnailRef = thumbnailRef;
    }

    public boolean isFeatured() {
        return hasTag(MediaTagConstants.FEATURED.getName());
    }
    
    @Override
    public int compareTo(AbstractMediaDTO o) {
        if(o instanceof VideoDTO) {
            VideoDTO otherVideo = (VideoDTO) o;
            boolean featured = isFeatured();
            if(otherVideo.isFeatured() != featured) {
                return featured ? -1 : 1;
            }
        }
        return super.compareTo(o);
    }
}
