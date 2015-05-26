package com.sap.sailing.gwt.ui.shared.media;

import java.net.URL;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.media.MimeType;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class VideoMetadataDTO extends AbstractMediaDTO {
    
    private static final long serialVersionUID = 1L;

    private int length;
    private String thumbnailRef;

    @SuppressWarnings("unused")
    private VideoMetadataDTO() {
    }

    @GwtIncompatible
    public VideoMetadataDTO(EventReferenceDTO eventRef, URL url, MimeType mimeType, String title) {
        this(eventRef, url.toString(), mimeType, title);
    }
    
    @GwtIncompatible
    public VideoMetadataDTO(EventReferenceDTO eventRef, String url, MimeType mimeType, String title) {
        super(eventRef, url, mimeType, title);
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
