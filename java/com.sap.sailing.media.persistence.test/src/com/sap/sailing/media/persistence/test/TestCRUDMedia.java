package com.sap.sailing.media.persistence.test;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack.MediaSubType;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack.MediaType;

import static junit.framework.Assert.assertNotNull;

import static org.junit.Assert.assertThat;

public class TestCRUDMedia extends AbstractMongoDBTest {

    private static final Logger logger = Logger.getLogger(TestCRUDMedia.class.getName());

    public TestCRUDMedia() throws UnknownHostException, MongoException {
        super();
    }

    @Test
    public void testCreateVideoTrack() {
        final String videoTitle = "Test Video";
        final String url = "http://localhost:8888/media/HTML5/1809147112001_1842870496001_SAP-Regatta-Day02-Final_libtheora.ogv";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        Date date = new Date();
        String mediaType = MediaType.VIDEO.name();
        String mediaSubType = MediaSubType.ogg.name();
        mongoDB.insertMediaTrack(videoTitle, url, date, mediaType, mediaSubType);
        DBMediaTrack videoTrack = mongoDB.loadMediaTrack(videoTitle);
        assertNotNull(videoTrack);
        assertThat(videoTrack.title, Is.is(videoTitle));
    }

    @Test(expected = RuntimeException.class)
    public void testFailReadMissingVideoTrack() {
        final String videoTitle = "Test Video";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        mongoDB.loadMediaTrack(videoTitle);
    }

    @Test
    public void testLoadAllVideoTracks() {
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        int testEntryCount = 10;
        storeNumberOfTestMediaTracks(mongoDB, testEntryCount);
        List<DBMediaTrack> allMediaTracks = mongoDB.loadAllMediaTracks();
        assertThat(allMediaTracks.size(), org.hamcrest.core.Is.is(testEntryCount));
    }

    private void storeNumberOfTestMediaTracks(MediaDB mongoDB, int count) {
        final String videoTitleTemplate = "Test Video ";
        final String url = "http://localhost:8888/media/HTML5/1809147112001_1842870496001_SAP-Regatta-Day02-Final_libtheora.ogv";
        Date date = new Date();
        String mediaType = MediaType.VIDEO.name();
        String mediaSubType = MediaSubType.ogg.name();

        for (int i = 0; i < count; i++) {
            mongoDB.insertMediaTrack(videoTitleTemplate + i, url, date, mediaType, mediaSubType);
        }
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteVideoTrack() {
        //insert test object
        final String videoTitle = "Test Video";
        final String url = "test";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        Date date = new Date();
        String mediaType = MediaType.VIDEO.name();
        String mediaSubType = MediaSubType.ogg.name();
        mongoDB.insertMediaTrack(videoTitle, url, date, mediaType, mediaSubType);
        
        //delete
        mongoDB.deleteMediaTrack(videoTitle);
        
        //assert not exists --> RuntimeException
        mongoDB.loadMediaTrack(videoTitle);
    }

}
