package com.sap.sailing.domain.persistence.media.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
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
    public String insertMediaTrack(String videoTitle, String url, Date startTime, int durationInMillis, String mimeType) {
        BasicDBObject dbVideo = new BasicDBObject();
        dbVideo.put(DbNames.Fields.MEDIA_TITLE.name(), videoTitle);
        dbVideo.put(DbNames.Fields.MEDIA_URL.name(), url);
        dbVideo.put(DbNames.Fields.STARTTIME.name(), startTime);
        dbVideo.put(DbNames.Fields.DURATION_IN_MILLIS.name(), durationInMillis);
        dbVideo.put(DbNames.Fields.MIME_TYPE.name(), mimeType);
        DBCollection dbVideos = getVideoCollection();
        dbVideos.insert(dbVideo);
        return ((ObjectId) dbVideo.get(DbNames.Fields._id.name())).toString();
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
    public DBMediaTrack loadMediaTrack(String dbId) {
        ObjectId objectId = new ObjectId(dbId);
        DBCursor cursor = getVideoCollection().find(new BasicDBObject(DbNames.Fields._id.name(), objectId));
        switch (cursor.count()) {
        case 0:
            throw new RuntimeException("Video not found with id: " + dbId);
        case 1:
            DBObject dbObject = cursor.next();
            DBMediaTrack dbMediaTrack = createMediaObjectFromDB(dbObject);
            return dbMediaTrack;
        default:
            throw new RuntimeException("Ambiguous videos not found with id: " + dbId);

        }
    }

    private DBMediaTrack createMediaObjectFromDB(DBObject dbObject) {
        String dbId = ((ObjectId) dbObject.get(DbNames.Fields._id.name())).toString();
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

        BasicDBObject update = new BasicDBObject();
        update.append("$set", new BasicDBObject(DbNames.Fields.MEDIA_TITLE.name(), title));

        getVideoCollection().update(updateQuery, update);
    }

    @Override
    public void updateUrl(String dbId, String url) {
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append(DbNames.Fields._id.name(), new ObjectId(dbId));

        BasicDBObject update = new BasicDBObject();
        update.append("$set", new BasicDBObject(DbNames.Fields.MEDIA_URL.name(), url));

        getVideoCollection().update(updateQuery, update);
    }

    @Override
    public void updateStartTime(String dbId, Date startTime) {
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append(DbNames.Fields._id.name(), new ObjectId(dbId));

        BasicDBObject update = new BasicDBObject();
        update.append("$set", new BasicDBObject(DbNames.Fields.STARTTIME.name(), startTime));

        getVideoCollection().update(updateQuery, update);
    }

    @Override
    public void updateDuration(String dbId, int durationInMillis) {
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append(DbNames.Fields._id.name(), new ObjectId(dbId));

        BasicDBObject update = new BasicDBObject();
        update.append("$set", new BasicDBObject(DbNames.Fields.DURATION_IN_MILLIS.name(), durationInMillis));

        getVideoCollection().update(updateQuery, update);
    }

    @Override
    public Collection<DBMediaTrack> queryOverlappingMediaTracks(Date rangeStart, Date rangeEnd) {
        BasicDBObject startTimeCondition = new BasicDBObject();
        startTimeCondition.put(DbNames.Fields.STARTTIME.name(), new BasicDBObject("$gt", rangeStart));
        startTimeCondition.put(DbNames.Fields.STARTTIME.name(), new BasicDBObject("$lt", rangeEnd));
        BasicDBObject endTimeCondition = new BasicDBObject();
        
        // Should actually be "AND greater than rangeStart - duration".
        // However, using values calculated on server side turns out to be a nightmare with mongodb.
        // Instead do the remaining filtering on client side...
        endTimeCondition.put(DbNames.Fields.STARTTIME.name(), new BasicDBObject("$lt", rangeStart));

        DBObject query = QueryBuilder.start().or(startTimeCondition, endTimeCondition).get();
        
        DBCursor cursor = getVideoCollection().find(query).sort(sortByStartTimeAndTitle);
        
        List<DBMediaTrack> result = new ArrayList<>(cursor.count());
        while (cursor.hasNext()) {
            DBMediaTrack resultCandidate = createMediaObjectFromDB(cursor.next());
            if (resultCandidate.durationInMillis > rangeStart.getTime() - resultCandidate.startTime.getTime()) {
                result.add(resultCandidate);
            }
        }
        return result;
    }
    
}
