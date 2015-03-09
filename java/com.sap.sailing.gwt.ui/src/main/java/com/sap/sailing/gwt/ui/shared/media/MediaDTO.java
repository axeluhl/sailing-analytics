package com.sap.sailing.gwt.ui.shared.media;

import java.util.ArrayList;

public class MediaDTO {
    
    private ArrayList<MediaEntryDTO> photos = new ArrayList<>();
    private ArrayList<MediaEntryDTO> videos = new ArrayList<>();

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
