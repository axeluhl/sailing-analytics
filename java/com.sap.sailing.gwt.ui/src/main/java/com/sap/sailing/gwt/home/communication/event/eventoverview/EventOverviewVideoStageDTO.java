package com.sap.sailing.gwt.home.communication.event.eventoverview;

import com.sap.sse.gwt.client.media.VideoDTO;

public class EventOverviewVideoStageDTO implements EventOverviewStageContentDTO {
    public enum Type {
        LIVESTREAM, HIGHLIGHTS, MEDIA
    }
    
    private Type type;
    private VideoDTO video;
    
    @SuppressWarnings("unused")
    private EventOverviewVideoStageDTO() {
    }

    public EventOverviewVideoStageDTO(Type type, VideoDTO video) {
        super();
        this.type = type;
        this.video = video;
    }
    
    public Type getType() {
        return type;
    }
    
    public VideoDTO getVideo() {
        return video;
    }
}
