package com.sap.sailing.gwt.home.communication.media;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

public class MediaDTO implements Serializable {
    private static final long serialVersionUID = 4932700214423641309L;
    
    private TreeSet<SailingImageDTO> photos = new TreeSet<>();
    private TreeSet<SailingVideoDTO> videos = new TreeSet<>();

    public Collection<SailingImageDTO> getPhotos() {
        return photos;
    }
    
    public void addPhoto(SailingImageDTO photo) {
        photos.add(photo);
    }
    
    public Collection<SailingVideoDTO> getVideos() {
        return videos;
    }
    
    public void addVideo(SailingVideoDTO video) {
        videos.add(video);
    }
}
