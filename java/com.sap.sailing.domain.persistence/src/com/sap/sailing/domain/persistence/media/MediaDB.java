package com.sap.sailing.domain.persistence.media;

import java.util.Collections;
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

    MediaDB TEST_STUB = new MediaDB() {

        @Override
        public String insertMediaTrack(String title, String url, Date startTime, int durationInMillis, String mimeType) {
            return "0";
        }

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
        public void updateStartTime(String dbId, Date startTime) {}

        @Override
        public void updateDuration(String dbId, int durationInMillis) {}
        
    };

    
}
