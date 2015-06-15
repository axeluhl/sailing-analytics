package com.sap.sse.common.media;

import java.net.URL;

public interface VideoDescriptor extends MediaDescriptor {
    Integer getLengthInSeconds();
    void setLengthInSeconds(Integer lengthInSeconds);
    
    URL getThumbnailURL();
    void setThumbnailURL(URL thumbnailURL);
}
