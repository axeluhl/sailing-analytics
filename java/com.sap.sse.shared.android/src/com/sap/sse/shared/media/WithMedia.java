package com.sap.sse.shared.media;

import java.util.List;

public interface WithMedia extends WithImages, WithVideos {
    ImageDescriptor findImageWithTag(String tagName);
    List<ImageDescriptor> findImagesWithTag(String tagName);
    boolean hasImageWithTag(String tagName);
    
    VideoDescriptor findVideoWithTag(String tagName);
    List<VideoDescriptor> findVideosWithTag(String tagName);
}
