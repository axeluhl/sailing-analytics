package com.sap.sailing.domain.abstractlog.race;

import java.net.URL;

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
    URL getImageURL();
    
}