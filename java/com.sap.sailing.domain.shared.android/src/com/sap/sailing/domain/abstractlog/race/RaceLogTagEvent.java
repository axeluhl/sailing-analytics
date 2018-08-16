package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sse.common.TimePoint;

public interface RaceLogTagEvent extends RaceLogEvent, Revokable {

    String getTag();
    
    String getUsername();
    
    String getComment();
    
    String getImageURL(); 
    
    void markAsRevoked(TimePoint revokedAt);
    
    TimePoint getRevokedAt();
}
