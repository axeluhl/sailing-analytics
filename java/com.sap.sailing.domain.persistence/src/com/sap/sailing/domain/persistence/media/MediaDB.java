package com.sap.sailing.domain.persistence.media;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Offers CRUD methods for mongo representation of media track objects.
 * 
 * @author Jens Rommel (d047974)
 * 
 */
public interface MediaDB {

    String insertMediaTrack(String title, String url, Date startTime, int durationInMillis, String mediaType, String mediaSubType);

    DBMediaTrack loadMediaTrack(String dbId);

    List<DBMediaTrack> loadAllMediaTracks();

    void deleteMediaTrack(String title);

    void updateStartTime(String title, Date startTime);

    void updateDuration(String dbId, int durationInMillis);

    Collection<DBMediaTrack> queryOverlappingMediaTracks(Date rangeStart, Date rangeEnd);

}
