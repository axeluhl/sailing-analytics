package com.sap.sailing.media.persistence.test;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.hamcrest.core.Is;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;

public class MediaDBTest extends AbstractMongoDBTest {

    public MediaDBTest() throws UnknownHostException, MongoException {
        super();
    }

    @Test
    public void testCreateVideoTrack() {
        final String videoTitle = "Test Video";
        final String url = "http://localhost:8888/media/HTML5/1809147112001_1842870496001_SAP-Regatta-Day02-Final_libtheora.ogv";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        Date date = new Date();
        int durationInMillis = 23;
        String mimeType = MimeType.ogv.name();
        String dbId = mongoDB.insertMediaTrack(videoTitle, url, date, durationInMillis, mimeType);
        assertNotNull(dbId);
        DBMediaTrack videoTrack = mongoDB.loadAllMediaTracks().iterator().next();
        assertNotNull(videoTrack);
        assertThat(videoTrack.dbId, Is.is(dbId));
        assertThat(videoTrack.title, Is.is(videoTitle));
    }
    
    @Test
    public void testCreateVideoTrackWithId() {
        final String dbId = new ObjectId().toStringMongod();
        final String videoTitle = "Test Video";
        final String url = "http://localhost:8888/media/HTML5/1809147112001_1842870496001_SAP-Regatta-Day02-Final_libtheora.ogv";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        Date date = new Date();
        int durationInMillis = 23;
        String mimeType = MimeType.ogv.name();
        mongoDB.insertMediaTrackWithId(dbId, videoTitle, url, date, durationInMillis, mimeType);
        DBMediaTrack videoTrack = mongoDB.loadAllMediaTracks().iterator().next();
        assertNotNull(videoTrack);
        assertThat(videoTrack.dbId, Is.is(dbId));
        assertThat(videoTrack.title, Is.is(videoTitle));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateVideoTrackWithNullId() {
        final String dbId = null;
        final String videoTitle = "Test Video";
        final String url = "http://localhost:8888/media/HTML5/1809147112001_1842870496001_SAP-Regatta-Day02-Final_libtheora.ogv";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        Date date = new Date();
        int durationInMillis = 23;
        String mimeType = MimeType.ogv.name();
        mongoDB.insertMediaTrackWithId(dbId, videoTitle, url, date, durationInMillis, mimeType);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateVideoTrackWithExistingId() {
        final String videoTitle = "Test Video";
        final String url = "http://localhost:8888/media/HTML5/1809147112001_1842870496001_SAP-Regatta-Day02-Final_libtheora.ogv";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        Date date = new Date();
        int durationInMillis = 23;
        String mimeType = MimeType.ogv.name();
        String dbId = mongoDB.insertMediaTrack(videoTitle, url, date, durationInMillis, mimeType);
        mongoDB.insertMediaTrackWithId(dbId, videoTitle, url, date, durationInMillis, mimeType);
    }
    
//    @Test
//    public void testImportVideoTrackToEmptyDb() throws Exception {
//        //insert test object
//        final String dbId = new ObjectId().toStringMongod();
//        final String title = "Test Video";
//        final String url = "test";
//        Date date = new Date();
//        int durationInMillis = 23;
//        String mimeType = MimeType.ogv.name();
//        
//        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
//
//        mongoDB.importMediaTrack(dbId, title, url, date, durationInMillis, mimeType);
//        
//        List<DBMediaTrack> allMediaTracks = mongoDB.loadAllMediaTracks();
//        assertThat(allMediaTracks.size(), is(1));
//        DBMediaTrack dbMediaTrack = allMediaTracks.get(0);
//        assertThat(dbMediaTrack.dbId, is(dbId));
//        assertThat(dbMediaTrack.title, is(title));
//        assertThat(dbMediaTrack.url, is(url));
//        assertThat(dbMediaTrack.startTime, is(date));
//        assertThat(dbMediaTrack.durationInMillis, is(durationInMillis));
//        assertThat(dbMediaTrack.mimeType, is(mimeType));
//    }
//
//    @Test(expected=NullPointerException.class)
//    public void testImportVideoTrackNullId() throws Exception {
//        //insert test object
//        final String dbId = null;
//        final String title = "Test Video";
//        final String url = "test";
//        Date date = new Date();
//        int durationInMillis = 23;
//        String mimeType = MimeType.ogv.name();
//        
//        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
//
//        mongoDB.importMediaTrack(dbId, title, url, date, durationInMillis, mimeType);
//    }
//
//    @Test
//    public void testImportVideoTrackToExistingTrack_ChangedTitle() throws Exception {
//        //insert test object
//        final String title = "Test Video";
//        final String url = "test";
//        Date date = new Date();
//        int durationInMillis = 23;
//        String mimeType = MimeType.ogv.name();
//        
//        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
//        String dbId = mongoDB.insertMediaTrack(title, url, date, durationInMillis, mimeType);
//        
//        String newTitle = title + "x";
//        boolean trackCreated = mongoDB.importMediaTrack(dbId, newTitle, url, date, durationInMillis, mimeType);
//        
//        assertThat(trackCreated, equalTo(true));
//        List<DBMediaTrack> allMediaTracks = mongoDB.loadAllMediaTracks();
//        assertThat(allMediaTracks.size(), is(1));
//        DBMediaTrack dbMediaTrack = allMediaTracks.get(0);
//        assertThat(dbMediaTrack.dbId, is(dbId));
//        assertThat(dbMediaTrack.title, is(newTitle));
//        assertThat(dbMediaTrack.url, is(url));
//        assertThat(dbMediaTrack.startTime, is(date));
//        assertThat(dbMediaTrack.durationInMillis, is(durationInMillis));
//        assertThat(dbMediaTrack.mimeType, is(mimeType));
//    }
//    
//    @Test
//    public void testImportVideoTrackToExistingTrack_ChangedUrl() throws Exception {
//        //insert test object
//        final String title = "Test Video";
//        final String url = "test";
//        Date date = new Date();
//        int durationInMillis = 23;
//        String mimeType = MimeType.ogv.name();
//        
//        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
//        String dbId = mongoDB.insertMediaTrack(title, url, date, durationInMillis, mimeType);
//        
//        String newUrl = url + "x";
//        boolean trackCreated = mongoDB.importMediaTrack(dbId, title, newUrl, date, durationInMillis, mimeType);
//        
//        assertThat(trackCreated, equalTo(true));
//        List<DBMediaTrack> allMediaTracks = mongoDB.loadAllMediaTracks();
//        assertThat(allMediaTracks.size(), is(1));
//        DBMediaTrack dbMediaTrack = allMediaTracks.get(0);
//        assertThat(dbMediaTrack.dbId, is(dbId));
//        assertThat(dbMediaTrack.title, is(title));
//        assertThat(dbMediaTrack.url, is(newUrl));
//        assertThat(dbMediaTrack.startTime, is(date));
//        assertThat(dbMediaTrack.durationInMillis, is(durationInMillis));
//        assertThat(dbMediaTrack.mimeType, is(mimeType));
//    }
//    
//    @Test
//    public void testImportVideoTrackToExistingTrack_ChangedStartTime() throws Exception {
//        //insert test object
//        final String title = "Test Video";
//        final String url = "test";
//        Date startTime = new Date();
//        int durationInMillis = 23;
//        String mimeType = MimeType.ogv.name();
//        
//        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
//        String dbId = mongoDB.insertMediaTrack(title, url, startTime, durationInMillis, mimeType);
//        
//        Date newStartTime = new Date(startTime.getTime() + 1);
//        boolean trackCreated = mongoDB.importMediaTrack(dbId, title, url, newStartTime, durationInMillis, mimeType);
//        
//        assertThat(trackCreated, equalTo(true));
//        List<DBMediaTrack> allMediaTracks = mongoDB.loadAllMediaTracks();
//        assertThat(allMediaTracks.size(), is(1));
//        DBMediaTrack dbMediaTrack = allMediaTracks.get(0);
//        assertThat(dbMediaTrack.dbId, is(dbId));
//        assertThat(dbMediaTrack.title, is(title));
//        assertThat(dbMediaTrack.url, is(url));
//        assertThat(dbMediaTrack.startTime, is(newStartTime));
//        assertThat(dbMediaTrack.durationInMillis, is(durationInMillis));
//        assertThat(dbMediaTrack.mimeType, is(mimeType));
//    }
//    
//    @Test
//    public void testImportVideoTrackToExistingTrack_Changedduration() throws Exception {
//        //insert test object
//        final String title = "Test Video";
//        final String url = "test";
//        Date startTime = new Date();
//        int durationInMillis = 23;
//        String mimeType = MimeType.ogv.name();
//        
//        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
//        String dbId = mongoDB.insertMediaTrack(title, url, startTime, durationInMillis, mimeType);
//        
//        int newDurationInMillis = durationInMillis + 1;
//        boolean trackCreated = mongoDB.importMediaTrack(dbId, title, url, startTime, newDurationInMillis, mimeType);
//        
//        assertThat(trackCreated, equalTo(true));
//        List<DBMediaTrack> allMediaTracks = mongoDB.loadAllMediaTracks();
//        assertThat(allMediaTracks.size(), is(1));
//        DBMediaTrack dbMediaTrack = allMediaTracks.get(0);
//        assertThat(dbMediaTrack.dbId, is(dbId));
//        assertThat(dbMediaTrack.title, is(title));
//        assertThat(dbMediaTrack.url, is(url));
//        assertThat(dbMediaTrack.startTime, is(startTime));
//        assertThat(dbMediaTrack.durationInMillis, is(newDurationInMillis));
//        assertThat(dbMediaTrack.mimeType, is(mimeType));
//    }
//    
    @Test
    public void testLoadAllVideoTracks() {
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        int testEntryCount = 10;
        storeNumberOfTestMediaTracks(mongoDB, testEntryCount);
        List<DBMediaTrack> allMediaTracks = mongoDB.loadAllMediaTracks();
        assertThat(allMediaTracks.size(), is(testEntryCount));
    }

    private void storeNumberOfTestMediaTracks(MediaDB mongoDB, int count) {
        final String videoTitleTemplate = "Test Video ";
        final String url = "http://localhost:8888/media/HTML5/1809147112001_1842870496001_SAP-Regatta-Day02-Final_libtheora.ogv";
        Date date = new Date();
        int durationInMillis = 23;
        String mimeType = MimeType.ogv.name();

        for (int i = 0; i < count; i++) {
            mongoDB.insertMediaTrack(videoTitleTemplate + i, url, date, durationInMillis, mimeType);
        }
    }

    @Test
    public void testDeleteVideoTrack() {
        //insert test object
        final String videoTitle = "Test Video";
        final String url = "test";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        Date date = new Date();
        int durationInMillis = 23;
        String mimeType = MimeType.ogv.name();
        String dbId = mongoDB.insertMediaTrack(videoTitle, url, date, durationInMillis, mimeType);
        
        //delete
        mongoDB.deleteMediaTrack(dbId);
        
        //assert not exists --> RuntimeException
        assertThat(mongoDB.loadAllMediaTracks().size(), is(0));
    }

    @Test
    public void testUpdateStartTime() {
        //insert test object
        final String videoTitle = "Test Video";
        final String url = "test";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        Date originalDate = new Date();
        int durationInMillis = 23;
        String mimeType = MimeType.ogv.name();
        String dbId = mongoDB.insertMediaTrack(videoTitle, url, originalDate, durationInMillis, mimeType);
        
        //update with new date
        Date newDate = new Date(originalDate.getTime() + 1000);
        mongoDB.updateStartTime(dbId, newDate);
        
        //assert start time is updated and everything else preserved
        DBMediaTrack videoTrack = mongoDB.loadAllMediaTracks().iterator().next();
        assertThat(videoTrack.startTime, is(newDate));
        assertThat(videoTrack.durationInMillis, is(durationInMillis));
        assertThat(videoTrack.title, is(videoTitle));
        assertThat(videoTrack.url, is(url));
        assertThat(videoTrack.mimeType, is(mimeType));
    }
    
}
