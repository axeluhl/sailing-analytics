package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.Revokable;

public interface RaceLogTagEvent extends RaceLogEvent, Revokable {

    String getTag();
    
    String getUsername();
    
    String getComment();
    
    String getImageURL(); 
    
    boolean isPublic();
}
