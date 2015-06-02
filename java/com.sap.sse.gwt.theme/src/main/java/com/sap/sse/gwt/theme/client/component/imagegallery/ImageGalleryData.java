package com.sap.sse.gwt.theme.client.component.imagegallery;

import java.util.ArrayList;
import java.util.List;

public class ImageGalleryData {
    private String name;
    private List<ImageDescriptor> images;
    
    public ImageGalleryData() {
        this("", new ArrayList<ImageDescriptor>());
    }

    public ImageGalleryData(String name) {
        this(name, new ArrayList<ImageDescriptor>());
    }

    public ImageGalleryData(String name, List<ImageDescriptor> images) {
        this.name = name;
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ImageDescriptor> getImages() {
        return images;
    }

    public void setImages(List<ImageDescriptor> images) {
        this.images = images;
    }
}
