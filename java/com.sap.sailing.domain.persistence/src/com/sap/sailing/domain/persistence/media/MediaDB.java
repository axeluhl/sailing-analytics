package com.sap.sailing.domain.persistence.media;

import java.util.Date;
import java.util.List;

import com.mongodb.DBObject;

/**
 * Offers CRUD methods for mongo representation of media track objects.
 * 
 * @author Jens Rommel (d047974)
 * 
 */
public interface MediaDB {

    void insertMediaTrack(String videoTitle, String url, Date startTime, String mediaType, String mediaSubType);

    DBMediaTrack loadMediaTrack(String videoTitle);

    List<DBMediaTrack> loadAllMediaTracks();

    void deleteMediaTrack(String title);

}
