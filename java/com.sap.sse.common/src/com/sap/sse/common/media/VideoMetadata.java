package com.sap.sse.common.media;

public interface VideoMetadata extends MediaMetadata {
    public int getLengthInSeconds();
    
    public String getThumbnailRef();
}
