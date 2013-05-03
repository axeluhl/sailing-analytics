package com.sap.sailing.server.replication.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.server.operationaltransformation.AddMediaTrackBatchOperation;
import com.sap.sailing.server.operationaltransformation.AddMediaTrackOperation;
import com.sap.sailing.server.operationaltransformation.RemoveMediaTrackOperation;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackOperation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import static org.hamcrest.core.Is.is;

public class MediaReplicationTest extends AbstractServerReplicationTest {

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
    public void testBasicInitialLoad() throws Exception {
        assertNotSame(master, replica);
        assertEquals(master.getAllMediaTracks().size(), replica.getAllMediaTracks().size());
    }
    
    @Test
    public void testAddMediaTrackReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack("1");
        AddMediaTrackOperation addMediaTrackOperation = new AddMediaTrackOperation(mediaTrack);
        assertThat(master.getAllMediaTracks().size(), is(0));
        assertThat(replica.getAllMediaTracks().size(), is(0));
        master.apply(addMediaTrackOperation);
        assertThat(master.getAllMediaTracks().size(), is(1));
        Thread.sleep(500); // wait for JMS to deliver the message and the message to be applied
        assertThat(replica.getAllMediaTracks().size(), is(1));
    }

    @Test
    public void testDeleteMediaTrackReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack("1");
        AddMediaTrackOperation addMediaTrackOperation = new AddMediaTrackOperation(mediaTrack);
        master.apply(addMediaTrackOperation);
        RemoveMediaTrackOperation removeMediaTrackOperation = new RemoveMediaTrackOperation(mediaTrack);
        master.apply(removeMediaTrackOperation);
        Thread.sleep(500); // wait for JMS to deliver the message and the message to be applied
        assertThat(replica.getAllMediaTracks().size(), is(0));
    }

    @Test
    public void testaddMediaTrackBatchReplication() throws InterruptedException {
        MediaTrack mediaTrack1 = createMediaTrack("1");
        MediaTrack mediaTrack2 = createMediaTrack("2");
        Collection<MediaTrack> mediaTrackBatch = Arrays.asList(new MediaTrack[] {mediaTrack1, mediaTrack2});
        AddMediaTrackBatchOperation addMediaTrackBatchOperation = new AddMediaTrackBatchOperation(mediaTrackBatch);
        master.apply(addMediaTrackBatchOperation);
        Thread.sleep(500); // wait for JMS to deliver the message and the message to be applied
        assertThat(replica.getAllMediaTracks().size(), is(2));
    }

    @Test
    public void testUpdateMediaTrackTitleReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack("1");
        AddMediaTrackOperation addMediaTrackOperation = new AddMediaTrackOperation(mediaTrack);
        master.apply(addMediaTrackOperation);
        mediaTrack.title = mediaTrack.title + "x";
        UpdateMediaTrackOperation updateMediaTrackOperation = new UpdateMediaTrackOperation(mediaTrack);
        master.apply(updateMediaTrackOperation);
        Thread.sleep(500); // wait for JMS to deliver the message and the message to be applied
        assertThat(replica.getAllMediaTracks().iterator().next().title, is(mediaTrack.title));
    }

    @Test
    public void testUpdateMediaTrackUrlReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack("1");
        AddMediaTrackOperation addMediaTrackOperation = new AddMediaTrackOperation(mediaTrack);
        master.apply(addMediaTrackOperation);
        mediaTrack.url = mediaTrack.url + "x";
        UpdateMediaTrackOperation updateMediaTrackOperation = new UpdateMediaTrackOperation(mediaTrack);
        master.apply(updateMediaTrackOperation);
        Thread.sleep(500); // wait for JMS to deliver the message and the message to be applied
        assertThat(replica.getAllMediaTracks().iterator().next().url, is(mediaTrack.url));
    }

    @Test
    public void testUpdateMediaTrackStartTimeReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack("1");
        AddMediaTrackOperation addMediaTrackOperation = new AddMediaTrackOperation(mediaTrack);
        master.apply(addMediaTrackOperation);
        mediaTrack.startTime = new Date(mediaTrack.startTime.getTime() + 1000);
        UpdateMediaTrackOperation updateMediaTrackOperation = new UpdateMediaTrackOperation(mediaTrack);
        master.apply(updateMediaTrackOperation);
        Thread.sleep(500); // wait for JMS to deliver the message and the message to be applied
        assertThat(replica.getAllMediaTracks().iterator().next().startTime, is(mediaTrack.startTime));
    }

    @Test
    public void testUpdateMediaTrackDurationReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack("1");
        AddMediaTrackOperation addMediaTrackOperation = new AddMediaTrackOperation(mediaTrack);
        master.apply(addMediaTrackOperation);
        mediaTrack.durationInMillis = mediaTrack.durationInMillis + 1000;
        UpdateMediaTrackOperation updateMediaTrackOperation = new UpdateMediaTrackOperation(mediaTrack);
        master.apply(updateMediaTrackOperation);
        Thread.sleep(500); // wait for JMS to deliver the message and the message to be applied
        assertThat(replica.getAllMediaTracks().iterator().next().durationInMillis, is(mediaTrack.durationInMillis));
    }

    @Test
    public void testUpdateMediaTrackMimeTypeReplication() throws InterruptedException {
        MediaTrack mediaTrack = createMediaTrack("1");
        AddMediaTrackOperation addMediaTrackOperation = new AddMediaTrackOperation(mediaTrack);
        master.apply(addMediaTrackOperation);
        mediaTrack.mimeType = MimeType.mp3;
        UpdateMediaTrackOperation updateMediaTrackOperation = new UpdateMediaTrackOperation(mediaTrack);
        master.apply(updateMediaTrackOperation);
        Thread.sleep(500); // wait for JMS to deliver the message and the message to be applied
        assertThat(replica.getAllMediaTracks().iterator().next().mimeType, is(mediaTrack.mimeType));
    }

}
