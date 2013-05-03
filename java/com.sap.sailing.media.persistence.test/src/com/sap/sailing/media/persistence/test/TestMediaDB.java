package com.sap.sailing.media.persistence.test;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import static org.hamcrest.core.Is.is;

public class TestMediaDB extends AbstractMongoDBTest {

    public TestMediaDB() throws UnknownHostException, MongoException {
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
        DBMediaTrack videoTrack = mongoDB.loadAllMediaTracks().iterator().next();
        assertNotNull(videoTrack);
        assertThat(videoTrack.dbId, Is.is(dbId));
        assertThat(videoTrack.title, Is.is(videoTitle));
    }

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
