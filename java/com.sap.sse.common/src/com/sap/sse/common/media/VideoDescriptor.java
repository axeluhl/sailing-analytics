package com.sap.sse.common.media;

import java.net.URL;

public interface VideoDescriptor extends MediaDescriptor {
    public int getLengthInSeconds();
    
    public URL getThumbnailURL();
}
