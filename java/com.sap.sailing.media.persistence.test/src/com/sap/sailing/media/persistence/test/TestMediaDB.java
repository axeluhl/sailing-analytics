package com.sap.sailing.media.persistence.test;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.gwt.ui.client.shared.media.MediaTrack.MimeType;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestMediaDB extends AbstractMongoDBTest {

    private static final int FIFTEEN_MINUTES_IN_MILLIS = 15 * 60 * 1000;
    private static final int THIRTY_MINUTES_IN_MILLIS = 30 * 60 * 1000;
    private static final int ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;

//    private static final Logger logger = Logger.getLogger(TestCRUDMedia.class.getName());

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
        DBMediaTrack videoTrack = mongoDB.loadMediaTrack(dbId);
        assertNotNull(videoTrack);
        assertThat(videoTrack.title, Is.is(videoTitle));
    }

    @Test(expected = RuntimeException.class)
    public void testFailReadMissingVideoTrack() {
        final String dbId = "non-existing id";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        mongoDB.loadMediaTrack(dbId);
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

    @Test(expected=RuntimeException.class)
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
        mongoDB.deleteMediaTrack(videoTitle);
        
        //assert not exists --> RuntimeException
        mongoDB.loadMediaTrack(dbId);
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
        DBMediaTrack videoTrack = mongoDB.loadMediaTrack(dbId);
        assertThat(videoTrack.startTime, is(newDate));
        assertThat(videoTrack.durationInMillis, is(durationInMillis));
        assertThat(videoTrack.title, is(videoTitle));
        assertThat(videoTrack.url, is(url));
        assertThat(videoTrack.mimeType, is(mimeType));
    }
    
    
    @Test
    public void testQueryMediaTracksBetween_MatchingStartTimes_TrackLongerThanEndTime() {
        Date startTime = new Date();
        int duration = ONE_HOUR_IN_MILLIS;
        Date rangeStart = startTime;
        Date rangeEnd = new Date(rangeStart.getTime() + THIRTY_MINUTES_IN_MILLIS);

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }

    @Test
    public void testQueryMediaTracksBetween_MatchingStartTimes_TrackShorterThanEndTime() {
        Date startTime = new Date();
        int duration = THIRTY_MINUTES_IN_MILLIS;
        Date rangeStart = startTime;
        Date rangeEnd = new Date(rangeStart.getTime() + ONE_HOUR_IN_MILLIS);

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_StartsBeforeEndsAfterRange() {
        Date startTime = new Date();
        int duration = ONE_HOUR_IN_MILLIS;
        Date rangeStart = new Date(startTime.getTime() + FIFTEEN_MINUTES_IN_MILLIS);
        Date rangeEnd = new Date(rangeStart.getTime() + FIFTEEN_MINUTES_IN_MILLIS);

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_StartsAfterEndsBeforeRange() {
        Date rangeStart = new Date();
        Date rangeEnd = new Date(rangeStart.getTime() + ONE_HOUR_IN_MILLIS);
        Date startTime = new Date(rangeStart.getTime() + FIFTEEN_MINUTES_IN_MILLIS);
        int duration = FIFTEEN_MINUTES_IN_MILLIS;

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_StartsAfterEndsAfterRange() {
        Date rangeStart = new Date();
        Date rangeEnd = new Date(rangeStart.getTime() + THIRTY_MINUTES_IN_MILLIS);
        Date startTime = new Date(rangeStart.getTime() + FIFTEEN_MINUTES_IN_MILLIS);
        int duration = THIRTY_MINUTES_IN_MILLIS;

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_StartsBeforeEndsBeforeRange() {
        Date startTime = new Date();
        int duration = THIRTY_MINUTES_IN_MILLIS;
        Date rangeStart = new Date(startTime.getTime() + FIFTEEN_MINUTES_IN_MILLIS);
        Date rangeEnd = new Date(rangeStart.getTime() + THIRTY_MINUTES_IN_MILLIS);

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_TrackEndsBeforeRange() {
        Date startTime = new Date();
        int duration = THIRTY_MINUTES_IN_MILLIS;
        Date rangeStart = new Date(startTime.getTime() + duration + 1);
        Date rangeEnd = new Date(rangeStart.getTime() + ONE_HOUR_IN_MILLIS);

        assertNoOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_TrackStartsAfterRange() {
        Date rangeStart = new Date();
        Date rangeEnd = new Date(rangeStart.getTime() + THIRTY_MINUTES_IN_MILLIS);
        Date startTime = new Date(rangeStart.getTime() + ONE_HOUR_IN_MILLIS);
        int duration = THIRTY_MINUTES_IN_MILLIS;

        assertNoOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    private void assertOverlap(Date startTime, int oneHourInMillis, Date rangeStart, Date rangeEnd) {
        //insert test object
        final String videoTitle = "Test Video";
        final String url = "test";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        String mimeType = MimeType.ogv.name();
        String dbId = mongoDB.insertMediaTrack(videoTitle, url, startTime, oneHourInMillis, mimeType);
        
        Collection<DBMediaTrack> videoTracks = mongoDB.queryOverlappingMediaTracks(rangeStart, rangeEnd);
        assertThat(videoTracks.size(), is(1));
        assertThat(videoTracks.iterator().next().dbId, is(dbId));
    }
    
    private void assertNoOverlap(Date startTime, int oneHourInMillis, Date rangeStart, Date rangeEnd) {
        //insert test object
        final String videoTitle = "Test Video";
        final String url = "test";
        MediaDB mongoDB = MediaDBFactory.INSTANCE.getMediaDB(getMongoService());
        String mimeType = MimeType.ogv.name();
        mongoDB.insertMediaTrack(videoTitle, url, startTime, oneHourInMillis, mimeType);
        
        Collection<DBMediaTrack> videoTracks = mongoDB.queryOverlappingMediaTracks(rangeStart, rangeEnd);
        assertThat(videoTracks.size(), is(0));
    }
    
}
