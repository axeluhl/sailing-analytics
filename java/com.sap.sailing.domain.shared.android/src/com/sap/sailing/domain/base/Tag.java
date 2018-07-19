package com.sap.sailing.domain.base;

import com.sap.sse.common.TimePoint;

public interface Tag {
    
    public String getTag();
    public String getComment();
    public String getImageURL();
    public String getUsername();
    public TimePoint getRaceTimepoint();
}
