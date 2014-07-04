package com.sap.sailing.server.replication.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;

public class MediaReplicationTest extends AbstractServerReplicationTest {
      
    private void waitSomeTime() throws InterruptedException {
        Thread.sleep(1000); // wait for JMS to deliver the message and the message to be applied
    }
    
    /* util */
    private MediaTrack createMediaTrack() {
        String title = "title";
        String url = "url";
        Date startTime = new Date();
        int durationInMillis = 1;
        MimeType mimeType = MimeType.mp4;
        MediaTrack mediaTrack = new MediaTrack(title, url, startTime, durationInMillis, mimeType);
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
        mediaTrack.startTime = new Date(mediaTrack.startTime.getTime() + 1000);
        master.mediaTrackStartTimeChanged(mediaTrack);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().startTime, is(mediaTrack.startTime));
    }

    @Test
    public void testUpdateMediaTrackDurationReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack();
        master.mediaTrackAdded(mediaTrack);
        mediaTrack.durationInMillis = mediaTrack.durationInMillis + 1000;
        master.mediaTrackDurationChanged(mediaTrack);
        waitSomeTime();
        assertThat(replica.getAllMediaTracks().size(), is(1));
        assertThat(replica.getAllMediaTracks().iterator().next().durationInMillis, is(mediaTrack.durationInMillis));
    }

}
