package com.sap.sailing.domain.persistence.media.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;

public class MediaDBImpl implements MediaDB {

    private static Logger logger = Logger.getLogger(MediaDBImpl.class.getName());
    private final DB database;

    public MediaDBImpl(DB database) {
        super();
        this.database = database;
    }

    @Override
    public void insertMediaTrack(String videoTitle, String url, Date startTime, String mediaType, String mediaSubType) {
        BasicDBObject dbVideo = new BasicDBObject();
        dbVideo.put(DbNames.Fields.MEDIA_TITLE.name(), videoTitle);
        dbVideo.put(DbNames.Fields.MEDIA_URL.name(), url);
        dbVideo.put(DbNames.Fields.STARTTIME.name(), startTime);
        dbVideo.put(DbNames.Fields.MIME_TYPE.name(), mediaType);
        dbVideo.put(DbNames.Fields.MIME_SUBTYPE.name(), mediaSubType);
        DBCollection dbVideos = getVideoCollection();
        dbVideos.insert(dbVideo);
    }

    private DBCollection getVideoCollection() {
        try {
            DBCollection dbVideos = database.getCollection(DbNames.Collections.VIDEOS.name());
            dbVideos.ensureIndex(DbNames.Collections.VIDEOS.name());
            return dbVideos;
        } catch (NullPointerException e) {
            // sometimes, for reasons yet to be clarified, ensuring an index on the name field causes an NPE
            throw new RuntimeException(e);
        }
    }

    @Override
    public DBMediaTrack loadMediaTrack(String videoTitle) {
        DBCursor cursor = getVideoCollection().find(new BasicDBObject(DbNames.Fields.MEDIA_TITLE.name(), videoTitle));
        switch (cursor.count()) {
        case 0:
            throw new RuntimeException("Video not found with title: " + videoTitle);
        case 1:
            DBObject dbObject = cursor.next();
            DBMediaTrack dbMediaTrack = createMediaObjectFromDB(dbObject);
            return dbMediaTrack;
        default:
            throw new RuntimeException("Ambiguous videos not found with title: " + videoTitle);

        }
    }

    private DBMediaTrack createMediaObjectFromDB(DBObject dbObject) {
        String title = (String) dbObject.get(DbNames.Fields.MEDIA_TITLE.name());
        String url = (String) dbObject.get(DbNames.Fields.MEDIA_URL.name());
        Date startTime = (Date) dbObject.get(DbNames.Fields.STARTTIME.name());
        String mimeType = (String) dbObject.get(DbNames.Fields.MIME_TYPE.name());
        String mimeSubType = (String) dbObject.get(DbNames.Fields.MIME_SUBTYPE.name());
        DBMediaTrack dbMediaTrack = new DBMediaTrack(title, url, startTime, mimeType, mimeSubType);
        return dbMediaTrack;
    }

    @Override
    public List<DBMediaTrack> loadAllMediaTracks() {
        DBCursor cursor = getVideoCollection().find();
        List<DBMediaTrack> result = new ArrayList<>(cursor.count());
        while (cursor.hasNext()) {
            result.add(createMediaObjectFromDB(cursor.next()));
        }
        return result;
    }

    @Override
    public void deleteMediaTrack(String title) {
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(DbNames.Fields.MEDIA_TITLE.name(), title);
        getVideoCollection().remove(dbObject);
    }
}
