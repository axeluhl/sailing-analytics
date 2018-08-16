package com.sap.sse.gwt.client.media;

import java.util.Date;
import java.util.Map;

public class ToResizeImageDTO extends ImageDTO {

    Map<String, Boolean> toResizeMap;

    /** for GWT */
    protected ToResizeImageDTO() {
    }

    public ToResizeImageDTO(String imageRef, Date createdAtDate, Map<String, Boolean> toResizeMap) {
        super(imageRef, createdAtDate);
        this.toResizeMap = toResizeMap;
    }

    public boolean resizeForTag(String tag) {
        return toResizeMap.get(tag);
    }

    public Map<String, Boolean> getMap() {
        return toResizeMap;
    }
}