package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.Revokable;

public interface RaceLogTagEvent extends RaceLogEvent, Revokable {

    String getTag();
    
    /*
     * Comment is optional
     */
    String getComment();
    /*
     * Image URL in RaceLogTagEvents is optional
     */
    String getImageURL();
    
}