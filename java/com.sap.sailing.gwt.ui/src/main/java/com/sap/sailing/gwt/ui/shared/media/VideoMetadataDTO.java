package com.sap.sailing.gwt.ui.shared.media;

import java.net.URL;

import com.google.gwt.core.shared.GwtIncompatible;

public class VideoMetadataDTO extends VideoReferenceDTO {
    
    private String title;

    @SuppressWarnings("unused")
    private VideoMetadataDTO() {
    }

    @GwtIncompatible
    public VideoMetadataDTO(URL youtubeIdOrURL, String title) {
        this(youtubeIdOrURL.toString(), title);
    }
    
    public VideoMetadataDTO(String youtubeIdOrURL, String title) {
        super(youtubeIdOrURL);
    }

    public String getTitle() {
        return title;
    }
}
