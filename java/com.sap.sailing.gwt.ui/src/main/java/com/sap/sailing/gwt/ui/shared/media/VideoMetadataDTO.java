package com.sap.sailing.gwt.ui.shared.media;

import java.net.URL;

import com.google.gwt.core.shared.GwtIncompatible;

public class VideoMetadataDTO extends VideoReferenceDTO {
    
    private String title;

    @SuppressWarnings("unused")
    private VideoMetadataDTO() {
    }

    @GwtIncompatible
    public VideoMetadataDTO(URL videoURLOrYoutubeId, String title) {
        this(videoURLOrYoutubeId.toString(), title);
    }
    
    @GwtIncompatible
    public VideoMetadataDTO(String videoURLOrYoutubeId, String title) {
        super(videoURLOrYoutubeId);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
