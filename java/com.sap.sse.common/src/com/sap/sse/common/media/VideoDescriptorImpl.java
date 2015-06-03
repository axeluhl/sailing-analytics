package com.sap.sse.common.media;

import java.net.URL;

import com.sap.sse.common.TimePoint;

public class VideoDescriptorImpl extends AbstractMediaDescriptor implements VideoDescriptor {
    private static final long serialVersionUID = 2651747912466590862L;

    private Integer lengthInSeconds;

    /**
     * URL to thumbnail image. This information works as override for youtube videos or as missing thumbnail
     * information for other formats. It can be either a link to thumbnail or even be a data/url contaning the base64
     * encoded image.
     */
    private URL thumbnailURL;
    
    public VideoDescriptorImpl(URL url, MimeType mimeType, TimePoint createdAtDate) {
        super(url, mimeType, createdAtDate);
    }

    @Override
    public Integer getLengthInSeconds() {
        return lengthInSeconds;
    }

    public void setLengthInSeconds(Integer lengthInSeconds) {
        this.lengthInSeconds = lengthInSeconds;
    }

    @Override
    public URL getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(URL thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
}
