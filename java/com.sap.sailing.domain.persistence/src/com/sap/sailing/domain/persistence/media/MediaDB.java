package com.sap.sailing.domain.persistence.media;

import java.util.Date;
import java.util.List;

/**
 * Offers CRUD methods for mongo representation of media track objects.
 * 
 * @author Jens Rommel (d047974)
 * 
 */
public interface MediaDB {

    String insertMediaTrack(String title, String url, Date startTime, int durationInMillis, String mimeType);

    List<DBMediaTrack> loadAllMediaTracks();

    void deleteMediaTrack(String dbId);

    void updateTitle(String dbId, String title);

    void updateUrl(String dbId, String url);

    void updateStartTime(String dbId, Date startTime);

    void updateDuration(String dbId, int durationInMillis);

}
