package com.sap.sailing.domain.persistence.media;

import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.Duration;
import com.sap.sailing.domain.common.TimePoint;

/**
 * Offers CRUD methods for mongo representation of media track objects.
 * 
 * @author Jens Rommel (d047974)
 * 
 */
public interface MediaDB {

    /**
     * Stores a new track to the database, returning the db-generated id.
     */
    String insertMediaTrack(String title, String url, TimePoint startTime, Duration duration, String mimeType);

    /**
     * Stores a new track to the database, using the db id of the specified trackToImport.
     * @return true if a new track has been added to db, e.g. the specified dbId didn't exist.
     * @throws NullpointerException When trackToImport.dbId is null.
     * @throws IllegalArgumentException When track with specified dbId already exists.
     */
    void insertMediaTrackWithId(String dbId, String videoTitle, String url, TimePoint startTime, Duration duration, String mimeType);

    List<DBMediaTrack> loadAllMediaTracks();

    void deleteMediaTrack(String dbId);

    void updateTitle(String dbId, String title);

    void updateUrl(String dbId, String url);

    void updateStartTime(String dbId, TimePoint startTime);

    void updateDuration(String dbId, Duration duration);

    MediaDB TEST_STUB = new MediaDB() {

        @Override
        public String insertMediaTrack(String title, String url, TimePoint startTime, Duration duration, String mimeType) {
            return "0";
        }

        @Override
        public void insertMediaTrackWithId(String dbId, String videoTitle, String url, TimePoint startTime, Duration duration, String mimeType) {};
        
        @Override
        public List<DBMediaTrack> loadAllMediaTracks() {
            return Collections.emptyList(); 
        }

        @Override
        public void deleteMediaTrack(String dbId) {}

        @Override
        public void updateTitle(String dbId, String title) {}

        @Override
        public void updateUrl(String dbId, String url) {}

        @Override
        public void updateStartTime(String dbId, TimePoint startTime) {}

        @Override
        public void updateDuration(String dbId, Duration duration) {}
        
    };

}
