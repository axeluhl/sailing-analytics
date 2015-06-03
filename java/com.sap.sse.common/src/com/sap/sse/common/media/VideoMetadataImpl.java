package com.sap.sse.common.media;

import java.net.URL;
import java.util.Date;

public class VideoMetadataImpl extends AbstractMediaMetadata {
    private static final long serialVersionUID = 2651747912466590862L;

    private int lengthInSeconds;

    /**
     * URL to thumbnail image. This information works as override for youtube videos or as missing thumbnail
     * information for other formats. It can be either a link to thumbnail or even be a data/url contaning the base64
     * encoded image.
     */
    private URL thumbnailURL;
    
    public VideoMetadataImpl(URL url, MimeType mimeType, Date createdAtDate) {
        super(url, mimeType, createdAtDate);
    }

    public int getLengthInSeconds() {
        return lengthInSeconds;
    }

    public void setLengthInSeconds(int lengthInSeconds) {
        this.lengthInSeconds = lengthInSeconds;
    }

    public URL getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(URL thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
}
