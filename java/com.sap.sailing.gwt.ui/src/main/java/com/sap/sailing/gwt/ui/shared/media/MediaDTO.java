package com.sap.sailing.gwt.ui.shared.media;

import java.io.Serializable;
import java.util.ArrayList;

public class MediaDTO implements Serializable {
    private static final long serialVersionUID = 4932700214423641309L;
    
    private ArrayList<ImageMetadataDTO> photos = new ArrayList<>();
    private ArrayList<VideoMetadataDTO> videos = new ArrayList<>();

    public ArrayList<ImageMetadataDTO> getPhotos() {
        return photos;
    }
    
    public void addPhoto(ImageMetadataDTO photo) {
        photos.add(photo);
    }
    
    public ArrayList<VideoMetadataDTO> getVideos() {
        return videos;
    }
    
    public void addVideo(VideoMetadataDTO video) {
        videos.add(video);
    }
}
