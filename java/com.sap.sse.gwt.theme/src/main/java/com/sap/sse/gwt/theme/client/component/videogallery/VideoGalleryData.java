package com.sap.sse.gwt.theme.client.component.videogallery;

import java.util.ArrayList;
import java.util.List;

public class VideoGalleryData {
    private String name;
    private List<VideoDescriptor> videos;
    
    public VideoGalleryData() {
        this("", new ArrayList<VideoDescriptor>());
    }

    public VideoGalleryData(String name) {
        this(name, new ArrayList<VideoDescriptor>());
    }

    public VideoGalleryData(String name, List<VideoDescriptor> videos) {
        this.name = name;
        this.videos = videos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<VideoDescriptor> getVideos() {
        return videos;
    }

    public void setVideos(List<VideoDescriptor> videos) {
        this.videos = videos;
    }
}