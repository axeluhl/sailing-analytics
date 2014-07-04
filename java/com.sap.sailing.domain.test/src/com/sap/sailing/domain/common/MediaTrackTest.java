package com.sap.sailing.domain.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.sap.sailing.domain.common.media.MediaTrack;

public class MediaTrackTest {
    
    @Test
    public void testExactOverlap() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 1;
        
        Date startTime = new Date(mediaTrack.startTime.getTime());
        Date endTime = new Date(mediaTrack.deriveEndTime().getTime());
        assertTrue(mediaTrack.overlapsWith(startTime, endTime));
        
    }

    @Test
    public void testNoOverlapLeft() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 1;
        
        Date startTime = new Date(mediaTrack.startTime.getTime() + 2);
        Date endTime = new Date(startTime.getTime() + 1);
        assertFalse(mediaTrack.overlapsWith(startTime, endTime));
        
    }

    @Test
    public void testNoOverlapRight() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 1;
        
        Date startTime = new Date(mediaTrack.startTime.getTime() - 2);
        Date endTime = new Date(startTime.getTime() + 1);
        assertFalse(mediaTrack.overlapsWith(startTime, endTime));
        
    }

    @Test
    public void testPartialOverlapLeft() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 2;
        
        Date startTime = new Date(mediaTrack.startTime.getTime() - 1);
        Date endTime = new Date(startTime.getTime() + 2);
        assertTrue(mediaTrack.overlapsWith(startTime, endTime));
        
    }

    @Test
    public void testPartialOverlapRight() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 2;
        
        Date startTime = new Date(mediaTrack.startTime.getTime() + 1);
        Date endTime = new Date(startTime.getTime() + 2);
        assertTrue(mediaTrack.overlapsWith(startTime, endTime));
        
    }

    @Test
    public void testMediaFullyIncluded() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 1;
        
        Date startTime = new Date(mediaTrack.startTime.getTime() - 1);
        Date endTime = new Date(startTime.getTime() + 3);
        assertTrue(mediaTrack.overlapsWith(startTime, endTime));
        
    }

    @Test
    public void testMediaFullyIncluding() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 3;
        
        Date startTime = new Date(mediaTrack.startTime.getTime() + 1);
        Date endTime = new Date(startTime.getTime() + 1);
        assertTrue(mediaTrack.overlapsWith(startTime, endTime));
        
    }

    @Test
    public void testOverlapOpenEndStartingEarlier() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 1;
        
        Date startTime = new Date(mediaTrack.startTime.getTime() - 1);
        Date endTime = null; //--> open end
        assertTrue(mediaTrack.overlapsWith(startTime, endTime));
        
    }

    @Test
    public void testOverlapOpenEndStartingLater() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 2;
        
        Date startTime = new Date(mediaTrack.startTime.getTime() + 1);
        Date endTime = null; //--> open end
        assertTrue(mediaTrack.overlapsWith(startTime, endTime));
        
    }

    @Test
    public void testOpenEndNoOverlap() throws Exception {
        MediaTrack mediaTrack = new MediaTrack();
        mediaTrack.startTime = new Date();
        mediaTrack.durationInMillis = 1;
        
        Date startTime = new Date(mediaTrack.startTime.getTime() + 2);
        Date endTime = null; //--> open end
        assertFalse(mediaTrack.overlapsWith(startTime, endTime));
        
    }

}
