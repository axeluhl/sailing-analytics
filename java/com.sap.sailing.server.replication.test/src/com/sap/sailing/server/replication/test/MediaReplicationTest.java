package com.sap.sailing.server.replication.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.common.Duration;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsDurationImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;

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
        Set<RegattaAndRaceIdentifier> regattasAndRaces = new HashSet<RegattaAndRaceIdentifier>();
        regattasAndRaces.add(new RegattaNameAndRaceName("49er", "R1"));
        MediaTrack mediaTrack = new MediaTrack(title, url, startTime, duration, mimeType, regattasAndRaces);
        return mediaTrack;
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
        assertThat(replica.getAllMediaTracks().iterator().next().regattasAndRaces.size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().regattasAndRaces, is(mediaTrack.regattasAndRaces));
    }

    @Test
    public void testDeleteMediaTrackReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        master.mediaTrackDeleted(mediaTrack);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(0));
    }

    @Test
    public void testUpdateMediaTrackTitleReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        mediaTrack.title = mediaTrack.title + "x";
        master.mediaTrackTitleChanged(mediaTrack);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().title, is(mediaTrack.title));
    }

    @Test
    public void testUpdateMediaTrackUrlReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        mediaTrack.url = mediaTrack.url + "x";
        master.mediaTrackUrlChanged(mediaTrack);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().url, is(mediaTrack.url));
    }

    @Test
    public void testUpdateMediaTrackStartTimeReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        mediaTrack.startTime = mediaTrack.startTime.plus(1000);
        master.mediaTrackStartTimeChanged(mediaTrack);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().startTime, is(mediaTrack.startTime));
    }

    @Test
    public void testUpdateMediaTrackDurationReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        mediaTrack.duration = mediaTrack.duration.plus(1000);
        master.mediaTrackDurationChanged(mediaTrack);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().duration, is(mediaTrack.duration));
    }
    
    @Test
    public void testUpdateMediaTrackAddRacesReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        mediaTrack.regattasAndRaces.add(new RegattaNameAndRaceName("505", "R1"));
        master.mediaTrackRacesChanged(mediaTrack);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().regattasAndRaces.size(), is(2));
        assertThat(replica.getAllMediaTracks().iterator().next().regattasAndRaces, is(mediaTrack.regattasAndRaces));
    }
    
    @Test
    public void testUpdateMediaTrackDeleteRacesReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        mediaTrack.regattasAndRaces.remove(new RegattaNameAndRaceName("49er", "R1"));
        master.mediaTrackRacesChanged(mediaTrack);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().regattasAndRaces.size(), is(0));
        assertThat(replica.getAllMediaTracks().iterator().next().regattasAndRaces, is(mediaTrack.regattasAndRaces));
    }

}
