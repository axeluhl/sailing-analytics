package com.sap.sailing.gwt.ui.shared.media;

import java.io.Serializable;
import java.net.URL;
import java.util.HashSet;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.media.MediaCategory;
import com.sap.sailing.domain.common.media.MimeType;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class AbstractMediaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;

    private MimeType mimeType;

    private MediaCategory category;

    private HashSet<String> tags = new HashSet<String>();

    private EventReferenceDTO eventRef;

    private String sourceRef;


    protected AbstractMediaDTO() {
    }

    @GwtIncompatible
    public AbstractMediaDTO(EventReferenceDTO eventRef, URL url, MimeType mimeType, String title) {
        this(eventRef, url.toString(), mimeType, title);
    }
    
    @GwtIncompatible
    public AbstractMediaDTO(EventReferenceDTO eventRef, String sourceRef, MimeType mimeType, String title) {
        this.eventRef = eventRef;
        this.title = title;
        this.mimeType = mimeType;
        this.sourceRef = sourceRef;
    }

    public String getTitle() {
        return title;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public MediaCategory getCategory() {
        return category;
    }

    public HashSet<String> getTags() {
        return tags;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setCategory(MediaCategory category) {
        this.category = category;
    }

    public EventReferenceDTO getEventRef() {
        return eventRef;
    }

}
