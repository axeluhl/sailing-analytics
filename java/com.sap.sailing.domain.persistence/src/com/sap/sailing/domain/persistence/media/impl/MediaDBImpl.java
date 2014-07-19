package com.sap.sailing.domain.persistence.media.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;

/**
 * MongoDB Java API examples: http://blog.rasc.ch/?p=1096
 * 
 * @author D047974
 * 
 */
public class MediaDBImpl implements MediaDB {

    private static final int SORT_ASCENDING = 1;
    private static final int SORT_DESSCENDING = -1;
    // private static Logger logger = Logger.getLogger(MediaDBImpl.class.getName());
    private final DB database;
    private final BasicDBObject sortByStartTimeAndTitle;

    public MediaDBImpl(DB database) {
        super();
        this.database = database;
        sortByStartTimeAndTitle = new BasicDBObject();
        sortByStartTimeAndTitle.put(DbNames.Fields.STARTTIME.name(), SORT_DESSCENDING);
        sortByStartTimeAndTitle.put(DbNames.Fields.MEDIA_TITLE.name(), SORT_ASCENDING);
    }

    @Override
    public String insertMediaTrack(String title, String url, Date startTime, int durationInMillis, String mimeType) {
        BasicDBObject dbMediaTrack = new BasicDBObject();
        dbMediaTrack.put(DbNames.Fields.MEDIA_TITLE.name(), title);
        dbMediaTrack.put(DbNames.Fields.MEDIA_URL.name(), url);
        dbMediaTrack.put(DbNames.Fields.STARTTIME.name(), startTime);
        dbMediaTrack.put(DbNames.Fields.DURATION_IN_MILLIS.name(), durationInMillis);
        dbMediaTrack.put(DbNames.Fields.MIME_TYPE.name(), mimeType);
        DBCollection dbVideos = getVideoCollection();
        dbVideos.insert(dbMediaTrack);
        return ((ObjectId) dbMediaTrack.get(DbNames.Fields._id.name())).toStringMongod();
    }

    @Override
    public void insertMediaTrackWithId(String dbId, String title, String url, Date startTime, int durationInMillis, String mimeType) {
        BasicDBObject dbMediaTrack = new BasicDBObject();
        dbMediaTrack.put(DbNames.Fields._id.name(), new ObjectId(dbId));
        dbMediaTrack.put(DbNames.Fields.MEDIA_TITLE.name(), title);
        dbMediaTrack.put(DbNames.Fields.MEDIA_URL.name(), url);
        dbMediaTrack.put(DbNames.Fields.STARTTIME.name(), startTime);
        dbMediaTrack.put(DbNames.Fields.DURATION_IN_MILLIS.name(), durationInMillis);
        dbMediaTrack.put(DbNames.Fields.MIME_TYPE.name(), mimeType);
        DBCollection dbVideos = getVideoCollection();
        try {
            dbVideos.insert(dbMediaTrack);
        } catch (MongoException.DuplicateKey e) {
            throw new IllegalArgumentException("Duplicate key '" + dbId + "' caused an error when importing media (title: '" + title + "')", e);
        }
    }

    private DBCollection getVideoCollection() {
        try {
            DBCollection dbVideos = database.getCollection(DbNames.Collections.VIDEOS.name());
            dbVideos.setWriteConcern(WriteConcern.FSYNC_SAFE);
            dbVideos.ensureIndex(DbNames.Collections.VIDEOS.name());
            return dbVideos;
        } catch (NullPointerException e) {
            // sometimes, for reasons yet to be clarified, ensuring an index on the name field causes an NPE
            throw e;
        }
    }

    private DBMediaTrack createMediaObjectFromDB(DBObject dbObject) {
        String dbId = ((ObjectId) dbObject.get(DbNames.Fields._id.name())).toStringMongod();
        String title = (String) dbObject.get(DbNames.Fields.MEDIA_TITLE.name());
        String url = (String) dbObject.get(DbNames.Fields.MEDIA_URL.name());
        Date startTime = (Date) dbObject.get(DbNames.Fields.STARTTIME.name());
        Integer durationInMillis = (Integer) dbObject.get(DbNames.Fields.DURATION_IN_MILLIS.name());
        String mimeType = (String) dbObject.get(DbNames.Fields.MIME_TYPE.name());
        DBMediaTrack dbMediaTrack = new DBMediaTrack(dbId, title, url, startTime, durationInMillis == null ? 0 : durationInMillis, mimeType);
        return dbMediaTrack;
    }

    @Override
    public List<DBMediaTrack> loadAllMediaTracks() {
        DBCursor cursor = getVideoCollection().find().sort(sortByStartTimeAndTitle);
        List<DBMediaTrack> result = new ArrayList<>(cursor.count());
        while (cursor.hasNext()) {
            result.add(createMediaObjectFromDB(cursor.next()));
        }
        return result;
    }

    @Override
    public void deleteMediaTrack(String dbId) {
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(DbNames.Fields._id.name(), new ObjectId(dbId));
        getVideoCollection().remove(dbObject);
    }

    @Override
    public void updateTitle(String dbId, String title) {
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append(DbNames.Fields._id.name(), new ObjectId(dbId));

        BasicDBObject updateCommand = new BasicDBObject();
        updateCommand.append("$set", new BasicDBObject(DbNames.Fields.MEDIA_TITLE.name(), title));

        getVideoCollection().update(updateQuery, updateCommand);
    }

    @Override
    public void updateUrl(String dbId, String url) {
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append(DbNames.Fields._id.name(), new ObjectId(dbId));

        BasicDBObject updateCommand = new BasicDBObject();
        updateCommand.append("$set", new BasicDBObject(DbNames.Fields.MEDIA_URL.name(), url));

        getVideoCollection().update(updateQuery, updateCommand);
    }

    @Override
    public void updateStartTime(String dbId, Date startTime) {
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append(DbNames.Fields._id.name(), new ObjectId(dbId));

        BasicDBObject updateCommand = new BasicDBObject();
        updateCommand.append("$set", new BasicDBObject(DbNames.Fields.STARTTIME.name(), startTime));

        getVideoCollection().update(updateQuery, updateCommand);
    }

    @Override
    public void updateDuration(String dbId, int durationInMillis) {
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append(DbNames.Fields._id.name(), new ObjectId(dbId));

        BasicDBObject updateCommand = new BasicDBObject();
        updateCommand.append("$set", new BasicDBObject(DbNames.Fields.DURATION_IN_MILLIS.name(), durationInMillis));

        getVideoCollection().update(updateQuery, updateCommand);
    }

}
