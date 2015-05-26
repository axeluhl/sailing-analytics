package com.sap.sailing.server.replication.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MimeType;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MediaReplicationTest extends AbstractServerReplicationTest {
      
    private void waitSomeTime() throws InterruptedException {
        Thread.sleep(1000); // wait for JMS to deliver the message and the message to be applied
    }
    
    /* util */
    private MediaTrack createMediaTrack() {
        String title = "title";
        String url = "url";
        TimePoint startTime = MillisecondsTimePoint.now();
        Duration duration = MillisecondsDurationImpl.ONE_HOUR;
        MimeType mimeType = MimeType.mp4;
        Set<RegattaAndRaceIdentifier> assignedRaces = new HashSet<RegattaAndRaceIdentifier>();
        assignedRaces.add(new RegattaNameAndRaceName("49er", "R1"));
        MediaTrack mediaTrack = new MediaTrack(title, url, startTime, duration, mimeType, assignedRaces);
        return mediaTrack;
    }
    
    /* util */
    private MediaTrack cloneMediaTrack(MediaTrack mediaTrack) {
        MediaTrack clonedMediaTrack = new MediaTrack(mediaTrack.dbId, mediaTrack.title, mediaTrack.url, mediaTrack.startTime, mediaTrack.duration, mediaTrack.mimeType, mediaTrack.assignedRaces);
        return clonedMediaTrack;
    }
    
    @Test
    public void testBasicInitialLoad() throws Exception {
        assertNotSame(master, replica);
        assertEquals(master.getAllMediaTracks().size(), replica.getAllMediaTracks().size());
    }
    
    @Test
    public void testAddMediaTrackReplication() throws InterruptedException {
        assertThat(master.getAllMediaTracks().size(), is(0));
        assertThat(replica.getAllMediaTracks().size(), is(0));
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        assertThat(master.getAllMediaTracks().size(), is(1));
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().title, is(mediaTrack.title));
        assertThat(replica.getAllMediaTracks().iterator().next().url, is(mediaTrack.url));
        assertThat(replica.getAllMediaTracks().iterator().next().startTime, is(mediaTrack.startTime));
        assertThat(replica.getAllMediaTracks().iterator().next().duration, is(mediaTrack.duration));
        assertThat(replica.getAllMediaTracks().iterator().next().mimeType, is(mediaTrack.mimeType));
        assertThat(replica.getAllMediaTracks().iterator().next().assignedRaces.size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().assignedRaces, is(mediaTrack.assignedRaces));
    }

    @Test
    public void testDeleteMediaTrackReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        MediaTrack mediaTrackClone = cloneMediaTrack(mediaTrack);
        master.mediaTrackDeleted(mediaTrackClone);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(0));
    }

    @Test
    public void testUpdateMediaTrackTitleReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        MediaTrack mediaTrackClone = cloneMediaTrack(mediaTrack);
        mediaTrackClone.title = mediaTrack.title + "x";
        master.mediaTrackTitleChanged(mediaTrackClone);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().title, is(mediaTrack.title));
    }

    @Test
    public void testUpdateMediaTrackUrlReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        MediaTrack mediaTrackClone = cloneMediaTrack(mediaTrack);
        mediaTrackClone.url = mediaTrack.url + "x";
        master.mediaTrackUrlChanged(mediaTrackClone);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().url, is(mediaTrack.url));
    }

    @Test
    public void testUpdateMediaTrackStartTimeReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        MediaTrack mediaTrackClone = cloneMediaTrack(mediaTrack);
        mediaTrackClone.startTime = mediaTrack.startTime.plus(1000);
        master.mediaTrackStartTimeChanged(mediaTrackClone);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().startTime, is(mediaTrack.startTime));
    }

    @Test
    public void testUpdateMediaTrackDurationReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        MediaTrack mediaTrackClone = cloneMediaTrack(mediaTrack);
        mediaTrackClone.duration = mediaTrack.duration.plus(1000);
        master.mediaTrackDurationChanged(mediaTrackClone);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().duration, is(mediaTrack.duration));
    }
    
    @Test
    public void testUpdateMediaTrackAddRacesReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        MediaTrack mediaTrackClone = cloneMediaTrack(mediaTrack);
        mediaTrackClone.assignedRaces.add(new RegattaNameAndRaceName("505", "R1"));
        master.mediaTrackAssignedRacesChanged(mediaTrackClone);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().assignedRaces.size(), is(2));
        assertThat(replica.getAllMediaTracks().iterator().next().assignedRaces, is(mediaTrack.assignedRaces));
    }
    
    @Test
    public void testUpdateMediaTrackDeleteRacesReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        MediaTrack mediaTrackClone = cloneMediaTrack(mediaTrack);
        mediaTrackClone.assignedRaces.remove(new RegattaNameAndRaceName("49er", "R1"));
        master.mediaTrackAssignedRacesChanged(mediaTrackClone);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().assignedRaces.size(), is(0));
        assertThat(replica.getAllMediaTracks().iterator().next().assignedRaces, is(mediaTrack.assignedRaces));
    }

}
