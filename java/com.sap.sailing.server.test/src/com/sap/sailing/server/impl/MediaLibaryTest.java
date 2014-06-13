package com.sap.sailing.server.impl;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.server.impl.MediaLibrary.Interval;


public class MediaLibaryTest {
    
    private static final int FIFTEEN_MINUTES_IN_MILLIS = 15 * 60 * 1000;
    private static final int THIRTY_MINUTES_IN_MILLIS = 30 * 60 * 1000;
    private static final int ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;

    private MediaLibrary mediaLibary;

    @Before
    public void before() {
        this.mediaLibary = new MediaLibrary();
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
    
    @Test
    public void testCacheChangeStartTime() {
        MediaTrack originalMediaTrack = new MediaTrack();
        originalMediaTrack.dbId = "a";
        originalMediaTrack.startTime = new Date();
        originalMediaTrack.durationInMillis = 100;
        
        Date queryStartTime = new Date(originalMediaTrack.startTime.getTime() + 1);
        Date queryEndTime = new Date(originalMediaTrack.deriveEndTime().getTime() - 1);

        mediaLibary.addMediaTrack(originalMediaTrack);
        
        Set<MediaTrack> firstQueryResult = mediaLibary.findMediaTracksInTimeRange(queryStartTime, queryEndTime);
        assertThat(firstQueryResult.size(), is(1));
        
        MediaTrack changedMediaTrack = new MediaTrack();
        changedMediaTrack.dbId = "a";
        changedMediaTrack.startTime = new Date(originalMediaTrack.startTime.getTime() + 101);
        changedMediaTrack.durationInMillis = 100;
        mediaLibary.startTimeChanged(changedMediaTrack);
        
        Set<MediaTrack> secondQueryResult = mediaLibary.findMediaTracksInTimeRange(queryStartTime, queryEndTime);
        assertThat(secondQueryResult.size(), is(0));

        Set<MediaTrack> thirdQueryResult = mediaLibary.findMediaTracksInTimeRange(changedMediaTrack.startTime, changedMediaTrack.deriveEndTime());
        assertThat(thirdQueryResult.size(), is(1));

        
    }
    
    @Test
    public void testCacheRemoveMediaTrack() {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.dbId = "a";
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 100;
        
        Date originalStartTime = mediaTrack.startTime;
        Date originalEndTime = mediaTrack.deriveEndTime();

        mediaLibary.addMediaTrack(mediaTrack);
        
        Set<MediaTrack> firstQueryResult = mediaLibary.findMediaTracksInTimeRange(originalStartTime, originalEndTime);
        assertThat(firstQueryResult.size(), is(1));
        
        mediaLibary.deleteMediaTrack(mediaTrack);
        
        Set<MediaTrack> secondQueryResult = mediaLibary.findMediaTracksInTimeRange(originalStartTime, originalEndTime);
        assertThat(secondQueryResult.size(), is(0));

    }
    
    @Test
    public void testCacheAddSecondMediaTrackWithSameInterval() {
        MediaTrack firstMediaTrack = new MediaTrack();
        firstMediaTrack.dbId = "a";
        firstMediaTrack.startTime = new Date();
        firstMediaTrack.durationInMillis = 100;
        mediaLibary.addMediaTrack(firstMediaTrack);
        
        Date queryStartTime = new Date(firstMediaTrack.startTime.getTime() + 1);
        Date queryEndTime = new Date(firstMediaTrack.deriveEndTime().getTime() - 1);
        
        Set<MediaTrack> firstQueryResult = mediaLibary.findMediaTracksInTimeRange(queryStartTime, queryEndTime);
        assertThat(firstQueryResult.size(), is(1));
        
        MediaTrack secondMediaTrack = new MediaTrack();
        secondMediaTrack.dbId = "b";
        secondMediaTrack.startTime = firstMediaTrack.startTime;
        secondMediaTrack.durationInMillis = firstMediaTrack.durationInMillis;
        mediaLibary.addMediaTrack(secondMediaTrack);
        
        Set<MediaTrack> secondQueryResult = mediaLibary.findMediaTracksInTimeRange(queryStartTime, queryEndTime);
        assertThat(secondQueryResult.size(), is(2));

        Date uncachedStartTime = new Date(firstMediaTrack.startTime.getTime() + 2);
        Date uncachedEndTime = new Date(firstMediaTrack.deriveEndTime().getTime() - 2);

        Set<MediaTrack> thirdQueryResult = mediaLibary.findMediaTracksInTimeRange(uncachedStartTime, uncachedEndTime);
        assertThat(thirdQueryResult.size(), is(2));
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
    
    @Test
    public void testIntervalEqualsIdentical() throws Exception {
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1);
        Interval interval = new Interval(date1, date2);
        assertTrue(interval.equals(interval));
    }
    
    @Test
    public void testIntervalEqualsSame() throws Exception {
        Date date1_1 = new Date();
        Date date1_2 = new Date(date1_1.getTime() + 1);
        Date date2_1 = new Date(date1_1.getTime());
        Date date2_2 = new Date(date1_2.getTime());
        Interval interval1 = new Interval(date1_1, date1_2);
        Interval interval2 = new Interval(date2_1, date2_2);
        assertTrue(interval1.equals(interval2));
        assertEquals(interval1.hashCode(), interval2.hashCode());
    }
    
    @Test
    public void testIntervalEqualsNull() throws Exception {
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1);
        Interval interval = new Interval(date1, date2);
        assertFalse(interval.equals(null));
    }
    
    @Test
    public void testIntervalNotEquals() throws Exception {
        Date date1_1 = new Date();
        Date date1_2 = new Date(date1_1.getTime() + 1);
        Date date2_1 = new Date(date1_1.getTime() + 2);
        Date date2_2 = new Date(date1_1.getTime() + 3);
        Interval interval1 = new Interval(date1_1, date1_2);
        Interval interval2 = new Interval(date2_1, date2_2);
        assertFalse(interval1.equals(interval2));
    }
    
}
