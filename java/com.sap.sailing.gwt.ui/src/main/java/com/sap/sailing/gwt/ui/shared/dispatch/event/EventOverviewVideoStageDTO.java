package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.common.media.MimeType;

public class EventOverviewVideoStageDTO implements EventOverviewStageContentDTO {
    public enum Type {
        LIVESTREAM, HIGHLIGHTS, MEDIA
    }
    
    private Type type;
    
    private MimeType mimeType;
    
    private String source;
    
    @SuppressWarnings("unused")
    private EventOverviewVideoStageDTO() {
    }

    public EventOverviewVideoStageDTO(Type type, MimeType mimeType, String source) {
        super();
        this.type = type;
        this.mimeType = mimeType;
        this.source = source;
    }
    
    public Type getType() {
        return type;
    }
    
    public MimeType getMimeType() {
        return mimeType;
    }
    
    public String getSource() {
        return source;
    }
}
