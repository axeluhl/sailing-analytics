package com.sap.sailing.gwt.ui.shared.media;

import java.io.Serializable;
import java.util.ArrayList;

public class MediaDTO implements Serializable {
    private static final long serialVersionUID = 4932700214423641309L;
    
    private ArrayList<SailingImageDTO> photos = new ArrayList<>();
    private ArrayList<SailingVideoDTO> videos = new ArrayList<>();

    public ArrayList<SailingImageDTO> getPhotos() {
        return photos;
    }
    
    public void addPhoto(SailingImageDTO photo) {
        photos.add(photo);
    }
    
    public ArrayList<SailingVideoDTO> getVideos() {
        return videos;
    }
    
    public void addVideo(SailingVideoDTO video) {
        videos.add(video);
    }
}
