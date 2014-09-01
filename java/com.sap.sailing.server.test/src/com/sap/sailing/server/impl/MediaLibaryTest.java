package com.sap.sailing.server.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;

import static org.junit.Assert.*;

import static org.hamcrest.core.Is.*;


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
        TimePoint startTime = MillisecondsTimePoint.now();
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
        TimePoint startTime = MillisecondsTimePoint.now();
        int duration = ONE_HOUR_IN_MILLIS;
        TimePoint rangeStart = startTime;
        TimePoint rangeEnd = rangeStart.plus(THIRTY_MINUTES_IN_MILLIS);

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }

    @Test
    public void testQueryMediaTracksBetween_MatchingStartTimes_TrackShorterThanEndTime() {
        TimePoint startTime = MillisecondsTimePoint.now();
        int duration = THIRTY_MINUTES_IN_MILLIS;
        TimePoint rangeStart = startTime;
        TimePoint rangeEnd = rangeStart.plus(ONE_HOUR_IN_MILLIS);

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_StartsBeforeEndsAfterRange() {
        TimePoint startTime = MillisecondsTimePoint.now();
        int duration = ONE_HOUR_IN_MILLIS;
        TimePoint rangeStart = startTime.plus(FIFTEEN_MINUTES_IN_MILLIS);
        TimePoint rangeEnd = rangeStart.plus(FIFTEEN_MINUTES_IN_MILLIS);

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_StartsAfterEndsBeforeRange() {
        TimePoint rangeStart = MillisecondsTimePoint.now();
        TimePoint rangeEnd = rangeStart.plus(ONE_HOUR_IN_MILLIS);
        TimePoint startTime = rangeStart.plus(FIFTEEN_MINUTES_IN_MILLIS);
        int duration = FIFTEEN_MINUTES_IN_MILLIS;

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_StartsAfterEndsAfterRange() {
        TimePoint rangeStart = MillisecondsTimePoint.now();
        TimePoint rangeEnd = rangeStart.plus(THIRTY_MINUTES_IN_MILLIS);
        TimePoint startTime = rangeStart.plus(FIFTEEN_MINUTES_IN_MILLIS);
        int duration = THIRTY_MINUTES_IN_MILLIS;

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_StartsBeforeEndsBeforeRange() {
        TimePoint startTime = MillisecondsTimePoint.now();
        int duration = THIRTY_MINUTES_IN_MILLIS;
        TimePoint rangeStart = startTime.plus(FIFTEEN_MINUTES_IN_MILLIS);
        TimePoint rangeEnd = rangeStart.plus(THIRTY_MINUTES_IN_MILLIS);

        assertOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_TrackEndsBeforeRange() {
        TimePoint startTime = MillisecondsTimePoint.now();
        int duration = THIRTY_MINUTES_IN_MILLIS;
        TimePoint rangeStart = startTime.plus(duration + 1);
        TimePoint rangeEnd = rangeStart.plus(ONE_HOUR_IN_MILLIS);

        assertNoOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testQueryMediaTracksBetween_TrackStartsAfterRange() {
        TimePoint rangeStart = MillisecondsTimePoint.now();
        TimePoint rangeEnd = rangeStart.plus(THIRTY_MINUTES_IN_MILLIS);
        TimePoint startTime = rangeStart.plus(ONE_HOUR_IN_MILLIS);
        int duration = THIRTY_MINUTES_IN_MILLIS;

        assertNoOverlap(startTime, duration, rangeStart, rangeEnd);
    }
    
    @Test
    public void testCacheChangeStartTime() {
        MediaTrack originalMediaTrack = new MediaTrack();
        originalMediaTrack.dbId = "a";
        originalMediaTrack.startTime = MillisecondsTimePoint.now();
        originalMediaTrack.durationInMillis = 100;
        
        TimePoint queryStartTime = originalMediaTrack.startTime.plus(1);
        TimePoint queryEndTime = originalMediaTrack.deriveEndTime().minus(1);

        mediaLibary.addMediaTrack(originalMediaTrack);
        
        Set<MediaTrack> firstQueryResult = mediaLibary.findMediaTracksInTimeRange(queryStartTime, queryEndTime);
        assertThat(firstQueryResult.size(), is(1));
        
        MediaTrack changedMediaTrack = new MediaTrack();
        changedMediaTrack.dbId = "a";
        changedMediaTrack.startTime = originalMediaTrack.startTime.plus(101);
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
        mediaTrack.startTime = MillisecondsTimePoint.now();
        mediaTrack.durationInMillis = 100;
        
        TimePoint originalStartTime = mediaTrack.startTime;
        TimePoint originalEndTime = mediaTrack.deriveEndTime();

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
        firstMediaTrack.startTime = MillisecondsTimePoint.now();
        firstMediaTrack.durationInMillis = 100;
        mediaLibary.addMediaTrack(firstMediaTrack);
        
        TimePoint queryStartTime = firstMediaTrack.startTime.plus(1);
        TimePoint queryEndTime = firstMediaTrack.deriveEndTime().minus(1);
        
        Set<MediaTrack> firstQueryResult = mediaLibary.findMediaTracksInTimeRange(queryStartTime, queryEndTime);
        assertThat(firstQueryResult.size(), is(1));
        
        MediaTrack secondMediaTrack = new MediaTrack();
        secondMediaTrack.dbId = "b";
        secondMediaTrack.startTime = firstMediaTrack.startTime;
        secondMediaTrack.durationInMillis = firstMediaTrack.durationInMillis;
        mediaLibary.addMediaTrack(secondMediaTrack);
        
        Set<MediaTrack> secondQueryResult = mediaLibary.findMediaTracksInTimeRange(queryStartTime, queryEndTime);
        assertThat(secondQueryResult.size(), is(2));

        TimePoint uncachedStartTime = firstMediaTrack.startTime.plus(2);
        TimePoint uncachedEndTime = firstMediaTrack.deriveEndTime().minus(2);

        Set<MediaTrack> thirdQueryResult = mediaLibary.findMediaTracksInTimeRange(uncachedStartTime, uncachedEndTime);
        assertThat(thirdQueryResult.size(), is(2));
}
    
    private void assertOverlap(TimePoint startTime, int durationInMillis, TimePoint rangeStart, TimePoint rangeEnd) {
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
    
    private void assertNoOverlap(TimePoint startTime, int durationInMillis, TimePoint rangeStart, TimePoint rangeEnd) {
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
        TimePoint date1 = MillisecondsTimePoint.now();
        TimePoint date2 = date1.plus(1);
        TimeRange interval = new TimeRangeImpl(date1, date2);
        assertTrue(interval.equals(interval));
    }
    
    @Test
    public void testIntervalEqualsSame() throws Exception {
        TimePoint date1_1 = MillisecondsTimePoint.now();
        TimePoint date1_2 = date1_1.plus(1);
        TimePoint date2_1 = date1_1;
        TimePoint date2_2 = date1_2;
        TimeRange interval1 = new TimeRangeImpl(date1_1, date1_2);
        TimeRange interval2 = new TimeRangeImpl(date2_1, date2_2);
        assertTrue(interval1.equals(interval2));
        assertEquals(interval1.hashCode(), interval2.hashCode());
    }
    
    @Test
    public void testIntervalEqualsNull() throws Exception {
        TimePoint date1 = MillisecondsTimePoint.now();
        TimePoint date2 = date1.plus(1);
        TimeRange interval = new TimeRangeImpl(date1, date2);
        assertFalse(interval.equals(null));
    }
    
    @Test
    public void testIntervalNotEquals() throws Exception {
        TimePoint date1_1 = MillisecondsTimePoint.now();
        TimePoint date1_2 = date1_1.plus(1);
        TimePoint date2_1 = date1_1.plus(2);
        TimePoint date2_2 = date1_1.plus(3);
        TimeRange interval1 = new TimeRangeImpl(date1_1, date1_2);
        TimeRange interval2 = new TimeRangeImpl(date2_1, date2_2);
        assertFalse(interval1.equals(interval2));
    }
    
}
