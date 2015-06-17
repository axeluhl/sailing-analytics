package com.sap.sse.common.media;

public interface WithVideos {

    /**
     * Returns a non-<code>null</code> live but unmodifiable collection of video resources that can be
     * used to represent the event, e.g., on a web page.
     * 
     * @return a non-<code>null</code> value which may be empty
     */
    Iterable<VideoDescriptor> getVideos();

    /**
     * Replaces the {@link #getVideos() current contents of the video sequence by the videos in
     * <code>videos</code>.
     * 
     * @param videos
     *            if <code>null</code>, the internal sequence of videos is cleared but remains valid (non-
     *            <code>null</code>)
     */
    void setVideos(Iterable<VideoDescriptor> videos);

    void addVideo(VideoDescriptor video);
    
    void removeVideo(VideoDescriptor video);
}
