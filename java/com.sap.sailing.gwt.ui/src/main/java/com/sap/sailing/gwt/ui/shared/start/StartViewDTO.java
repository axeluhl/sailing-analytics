package com.sap.sailing.gwt.ui.shared.start;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaEntryDTO;

public class StartViewDTO implements IsSerializable {

    private ArrayList<EventStageDTO> stageEvents = new ArrayList<EventStageDTO>();

    private ArrayList<EventListEventDTO> recentEvents = new ArrayList<EventListEventDTO>();
    
    private ArrayList<MediaEntryDTO> photos = new ArrayList<>();
    private ArrayList<MediaEntryDTO> videos = new ArrayList<>();

    public List<EventStageDTO> getStageEvents() {
        return stageEvents;
    }
    
    public void addStageEvent(EventStageDTO event) {
        this.stageEvents.add(event);
    }

    public List<EventListEventDTO> getRecentEvents() {
        return recentEvents;
    }
    
    public void addRecentEvent(EventListEventDTO event) {
        this.recentEvents.add(event);
    }
    
    public ArrayList<MediaEntryDTO> getPhotos() {
        return photos;
    }
    
    public void addPhoto(MediaEntryDTO photo) {
        photos.add(photo);
    }
    
    public ArrayList<MediaEntryDTO> getVideos() {
        return videos;
    }
    
    public void addVideo(MediaEntryDTO video) {
        videos.add(video);
    }
}
