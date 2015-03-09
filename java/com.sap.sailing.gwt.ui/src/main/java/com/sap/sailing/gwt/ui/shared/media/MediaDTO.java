package com.sap.sailing.gwt.ui.shared.media;

import java.io.Serializable;
import java.util.ArrayList;

public class MediaDTO implements Serializable {
    private static final long serialVersionUID = 4932700214423641309L;
    
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
