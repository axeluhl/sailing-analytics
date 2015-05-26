package com.sap.sailing.gwt.ui.shared.media;

import java.net.URL;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.media.VideoMetadataDTO;

public class SailingVideoDTO extends VideoMetadataDTO {
    
    private static final long serialVersionUID = 1L;

    private EventReferenceDTO eventRef;

    @SuppressWarnings("unused")
    private SailingVideoDTO() {
    }

    @GwtIncompatible
    public SailingVideoDTO(EventReferenceDTO eventRef, URL url, MimeType mimeType, String title) {
        this(eventRef, url.toString(), mimeType, title);
        this.eventRef = eventRef;
    }
    
    @GwtIncompatible
    public SailingVideoDTO(EventReferenceDTO eventRef, String url, MimeType mimeType, String title) {
        super(url, mimeType, title);
        this.eventRef = eventRef;
    }

    public EventReferenceDTO getEventRef() {
        return eventRef;
    }

}
