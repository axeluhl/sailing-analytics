package com.sap.sailing.server.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.Replicator;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.core.Is.is;


public class MediaLibaryTest {
    
    private static final int FIFTEEN_MINUTES_IN_MILLIS = 15 * 60 * 1000;
    private static final int THIRTY_MINUTES_IN_MILLIS = 30 * 60 * 1000;
    private static final int ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;

    private MediaLibrary mediaLibary;
    private Replicator replicator;

    @Before
    public void before() {
        replicator = new Replicator() {

            @Override
            public <T> void replicate(RacingEventServiceOperation<T> operation) {
            }
            
        };
        this.mediaLibary = new MediaLibrary(replicator);
    }

    /* util */
    private MediaTrack createMediaTrack(String dbId) {
        String title = "title";
        String url = "url";
        Date startTime = new Date();
        int durationInMillis = 1;
        MimeType mimeType = MimeType.mp4;
        MediaTrack mediaTrack = new MediaTrack(dbId, title, url, startTime, durationInMillis, mimeType);
        return mediaTrack;
    }
    
    @Test
    public void testGetElementsFromEmtpy() throws Exception {
        assertThat(this.mediaLibary.allTracks().size(), is(0));
    }

    @Test
    public void testCreateOneElement() throws Exception {
        String dbId = "1234";
        MediaTrack mediaTrack = createMediaTrack(dbId);
        this.mediaLibary.addMediaTrack(mediaTrack);
        Iterator<MediaTrack> allMediaTracks = this.mediaLibary.allTracks().iterator();
        assertThat(allMediaTracks.hasNext(), is(true));
        MediaTrack item = allMediaTracks.next();
        assertThat(allMediaTracks.hasNext(), is(false));
        assertTrue(item.equals(mediaTrack));
        assertThat(item.title, is(mediaTrack.title));
        assertThat(item.url, is(mediaTrack.url));
        assertThat(item.startTime, is(mediaTrack.startTime));
        assertThat(item.durationInMillis, is(mediaTrack.durationInMillis));
        assertThat(item.mimeType, is(mediaTrack.mimeType));
        
    }

    @Test
    public void testCreateDeleteOneElement() throws Exception {
        String dbId = "1234";
        MediaTrack mediaTrack = createMediaTrack(dbId);
        this.mediaLibary.addMediaTrack(mediaTrack);
        this.mediaLibary.deleteMediaTrack(mediaTrack);
        Collection<MediaTrack> allMediaTracks = this.mediaLibary.allTracks();
        assertThat(allMediaTracks.size(), is(0));
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
    
    private void assertOverlap(Date startTime, int durationInMillis, Date rangeStart, Date rangeEnd) {
        //insert test object
        String dbId = "1234";
        String title = "title";
        String url = "url";
        MimeType mimeType = MimeType.mp4;
        MediaTrack mediaTrack = new MediaTrack(dbId, title, url, startTime, durationInMillis, mimeType);
        mediaLibary.addMediaTrack(mediaTrack);
        
        Collection<MediaTrack> mediaTracks = mediaLibary.findMediaTracksInTimeRange(rangeStart, rangeEnd);
        assertThat(mediaTracks.size(), is(1));
        assertThat(mediaTracks.iterator().next().dbId, is(dbId));
    }
    
    private void assertNoOverlap(Date startTime, int durationInMillis, Date rangeStart, Date rangeEnd) {
        //insert test object
        String dbId = "1234";
        String title = "title";
        String url = "url";
        MimeType mimeType = MimeType.mp4;
        MediaTrack mediaTrack = new MediaTrack(dbId, title, url, startTime, durationInMillis, mimeType);
        mediaLibary.addMediaTrack(mediaTrack);
        
        Collection<MediaTrack> mediaTracks = mediaLibary.findMediaTracksInTimeRange(rangeStart, rangeEnd);
        assertThat(mediaTracks.size(), is(0));
    }
    
}
