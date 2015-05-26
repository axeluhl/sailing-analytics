package com.sap.sse.common.media;

import java.net.URL;

import com.google.gwt.core.shared.GwtIncompatible;

public class VideoMetadataDTO extends AbstractMediaDTO {
    
    private static final long serialVersionUID = 1L;

    private int length;
    private String thumbnailRef;


    protected VideoMetadataDTO() {
    }

    @GwtIncompatible
    public VideoMetadataDTO(URL url, MimeType mimeType, String title) {
        this(url.toString(), mimeType, title);
    }
    
    @GwtIncompatible
    public VideoMetadataDTO(String url, MimeType mimeType, String title) {
        super(url, mimeType, title);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setThumbnailRef(String thumbnailRef) {
        this.thumbnailRef = thumbnailRef;
    }

    public String getThumbnailRef() {
        return thumbnailRef;
    }
}
