package com.sap.sailing.gwt.ui.shared.start;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.ui.shared.media.ImageMetadataDTO;
import com.sap.sailing.gwt.ui.shared.media.VideoMetadataDTO;

public class StartViewDTO implements IsSerializable {

    private ArrayList<EventStageDTO> stageEvents = new ArrayList<EventStageDTO>();

    private ArrayList<EventListEventDTO> recentEvents = new ArrayList<EventListEventDTO>();
    
    private ArrayList<ImageMetadataDTO> photos = new ArrayList<>();
    private HashSet<VideoMetadataDTO> videos = new HashSet<>();

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
    
    public ArrayList<ImageMetadataDTO> getPhotos() {
        return photos;
    }
    
    public void addPhoto(ImageMetadataDTO photo) {
        photos.add(photo);
    }
    
    public Collection<VideoMetadataDTO> getVideos() {
        return videos;
    }
    
    public void addVideo(VideoMetadataDTO video) {
        videos.add(video);
    }
}
