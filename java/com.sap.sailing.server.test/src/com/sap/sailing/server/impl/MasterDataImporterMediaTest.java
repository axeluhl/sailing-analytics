package com.sap.sailing.server.impl;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.never;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.server.RacingEventService;

public class MasterDataImporterMediaTest {
    
    private static final boolean NO_OVERRIDE = false;
    private static final boolean OVERRIDE = true;

    private RacingEventService racingEventService;
    private Collection<MediaTrack> mediaTracksToImport;
    
    @Before
    public void before() {
        mediaTracksToImport = new ArrayList<MediaTrack>();
    }

    private void createRacingEventService(DBMediaTrack... existingDbMediaTracks) {
        DomainObjectFactory domainObjectFactory = mock(DomainObjectFactory.class, Mockito.RETURNS_MOCKS);
        MongoObjectFactory mongoObjectFactory = mock(MongoObjectFactory.class);
        WindStore windStore = mock(WindStore.class);
        
        MediaDB mediaDb = mock(MediaDB.class);
        when(mediaDb.loadAllMediaTracks()).thenReturn(Arrays.asList(existingDbMediaTracks));
        
        racingEventService = spy(new RacingEventServiceImpl(domainObjectFactory, mongoObjectFactory, mediaDb, windStore));
    }
    
    @Test
    public void testEmptyImportList_NoOverride() throws Exception {
        createRacingEventService();
        racingEventService.mediaTracksImported(mediaTracksToImport, NO_OVERRIDE);

        verify(racingEventService, never()).mediaTrackAdded(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
    }

    @Test
    public void testEmptyImportList_WithOverride() throws Exception {
        createRacingEventService();
        racingEventService.mediaTracksImported(mediaTracksToImport, OVERRIDE);

        verify(racingEventService, never()).mediaTrackAdded(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
    }

    @Test
    public void testImportOneTrackToEmptyTarget_WithOverride() throws Exception {
        createRacingEventService();
        
        String dbId = new ObjectId().toStringMongod();
        MediaTrack mediaTrackToImport = new MediaTrack(dbId, "title", "url", new Date(), 0, MimeType.mp3);
        mediaTracksToImport.add(mediaTrackToImport);
        racingEventService.mediaTracksImported(mediaTracksToImport, OVERRIDE);
        
        Collection<MediaTrack> allMediaTracks = racingEventService.getAllMediaTracks();
        assertThat(allMediaTracks.size(), is(1));
        MediaTrack mediaTrack = allMediaTracks.iterator().next();
        assertThat(mediaTrack, sameInstance(mediaTrackToImport));
        
        verify(racingEventService).mediaTrackAdded(same(mediaTrackToImport));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
        
    }

    @Test
    public void testImportOneTrackToSameTarget_WithOverride() throws Exception {
        String dbId = new ObjectId().toStringMongod();
        MimeType mimeType = MimeType.mp3;
        DBMediaTrack existingMediaTrack = new DBMediaTrack(dbId, "title", "url", new Date(), 0, mimeType.name());
        createRacingEventService(existingMediaTrack);
        Collection<MediaTrack> allMediaTracksBeforeImport = racingEventService.getAllMediaTracks();
        
        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title, existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, mimeType);
        mediaTracksToImport.add(mediaTrackToImport);
        racingEventService.mediaTracksImported(mediaTracksToImport, OVERRIDE);

        Collection<MediaTrack> allMediaTracksAfterImport = racingEventService.getAllMediaTracks();
        assertThat(allMediaTracksAfterImport, is(allMediaTracksBeforeImport));
        
        verify(racingEventService, never()).mediaTrackAdded(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
    }

    @Test
    public void testImportOneTrackToExistingOtherTrack_WithOverride() throws Exception {
        String dbId = new ObjectId().toStringMongod();
        MimeType mimeType = MimeType.mp3;
        DBMediaTrack existingMediaTrack = new DBMediaTrack(dbId, "title", "url", new Date(), 0, mimeType.name());
        createRacingEventService(existingMediaTrack);

        String dbId2 = new ObjectId().toStringMongod();
        MediaTrack mediaTrackToImport = new MediaTrack(dbId2, existingMediaTrack.title, existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, mimeType);
        mediaTracksToImport.add(mediaTrackToImport);
        racingEventService.mediaTracksImported(mediaTracksToImport, OVERRIDE);
        
        Collection<MediaTrack> allMediaTracksAfterImport = racingEventService.getAllMediaTracks();
        assertThat(allMediaTracksAfterImport.size(), is(2));

        
        verify(racingEventService).mediaTrackAdded(same(mediaTrackToImport));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedTitle_WithOverride() throws Exception {
        String dbId = new ObjectId().toStringMongod();
        MimeType mimeType = MimeType.mp3;
        DBMediaTrack existingMediaTrack = new DBMediaTrack(dbId, "title", "url", new Date(), 0, mimeType.name());
        createRacingEventService(existingMediaTrack);

        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title + "x", existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, mimeType);
        assertThat(existingMediaTrack.title, is(not(mediaTrackToImport.title)));

        mediaTracksToImport.add(mediaTrackToImport);
        racingEventService.mediaTracksImported(mediaTracksToImport, OVERRIDE);
        
        Collection<MediaTrack> allMediaTracksAfterImport = racingEventService.getAllMediaTracks();
        assertThat(allMediaTracksAfterImport.size(), is(1));
        MediaTrack mediaTrack = allMediaTracksAfterImport.iterator().next();
        assertThat(mediaTrack.title, is(mediaTrackToImport.title));


        verify(racingEventService, never()).mediaTrackAdded(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService).mediaTrackTitleChanged(eq(mediaTrackToImport));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
    }

    @Test
    public void testImportOneTrackWithNullTitleToSameTargetWithTitle_WithOverride() throws Exception {
        String dbId = new ObjectId().toStringMongod();
        MimeType mimeType = MimeType.mp3;
        DBMediaTrack existingMediaTrack = new DBMediaTrack(dbId, "title", "url", new Date(), 0, mimeType.name());
        createRacingEventService(existingMediaTrack);

        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, null, existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, mimeType);
        assertThat(existingMediaTrack.title, is(not(mediaTrackToImport.title)));

        mediaTracksToImport.add(mediaTrackToImport);
        racingEventService.mediaTracksImported(mediaTracksToImport, OVERRIDE);
        
        Collection<MediaTrack> allMediaTracksAfterImport = racingEventService.getAllMediaTracks();
        assertThat(allMediaTracksAfterImport.size(), is(1));
        MediaTrack mediaTrack = allMediaTracksAfterImport.iterator().next();
        assertThat(mediaTrack.title, is(mediaTrackToImport.title));

        verify(racingEventService, never()).mediaTrackAdded(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService).mediaTrackTitleChanged(eq(mediaTrackToImport));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedTitle_NoOverride() throws Exception {
        String dbId = new ObjectId().toStringMongod();
        MimeType mimeType = MimeType.mp3;
        DBMediaTrack existingMediaTrack = new DBMediaTrack(dbId, "title", "url", new Date(), 0, mimeType.name());
        createRacingEventService(existingMediaTrack);

        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title + "x", existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, mimeType);
        assertThat(existingMediaTrack.title, is(not(mediaTrackToImport.title)));

        mediaTracksToImport.add(mediaTrackToImport);
        racingEventService.mediaTracksImported(mediaTracksToImport, NO_OVERRIDE);
        
        Collection<MediaTrack> allMediaTracksAfterImport = racingEventService.getAllMediaTracks();
        assertThat(allMediaTracksAfterImport.size(), is(1));
        MediaTrack mediaTrack = allMediaTracksAfterImport.iterator().next();
        assertThat(mediaTrack.title, is(existingMediaTrack.title));

        verify(racingEventService, never()).mediaTrackAdded(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedUrl_WithOverride() throws Exception {
        String dbId = new ObjectId().toStringMongod();
        MimeType mimeType = MimeType.mp3;
        DBMediaTrack existingMediaTrack = new DBMediaTrack(dbId, "title", "url", new Date(), 0, mimeType.name());
        createRacingEventService(existingMediaTrack);

        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title, existingMediaTrack.url + "x", existingMediaTrack.startTime, existingMediaTrack.durationInMillis, mimeType);
        assertThat(existingMediaTrack.url, is(not(mediaTrackToImport.url)));

        mediaTracksToImport.add(mediaTrackToImport);
        racingEventService.mediaTracksImported(mediaTracksToImport, OVERRIDE);
        
        Collection<MediaTrack> allMediaTracksAfterImport = racingEventService.getAllMediaTracks();
        assertThat(allMediaTracksAfterImport.size(), is(1));
        MediaTrack mediaTrack = allMediaTracksAfterImport.iterator().next();
        assertThat(mediaTrack.url, is(mediaTrackToImport.url));

        verify(racingEventService, never()).mediaTrackAdded(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService).mediaTrackUrlChanged(eq(mediaTrackToImport));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedStarttime_WithOverride() throws Exception {
        String dbId = new ObjectId().toStringMongod();
        MimeType mimeType = MimeType.mp3;
        DBMediaTrack existingMediaTrack = new DBMediaTrack(dbId, "title", "url", new Date(), 0, mimeType.name());
        createRacingEventService(existingMediaTrack);

        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title, existingMediaTrack.url, new Date(existingMediaTrack.startTime.getTime() + 1), existingMediaTrack.durationInMillis, mimeType);
        assertThat(existingMediaTrack.startTime, is(not(mediaTrackToImport.startTime)));

        mediaTracksToImport.add(mediaTrackToImport);
        racingEventService.mediaTracksImported(mediaTracksToImport, OVERRIDE);
        
        Collection<MediaTrack> allMediaTracksAfterImport = racingEventService.getAllMediaTracks();
        assertThat(allMediaTracksAfterImport.size(), is(1));
        MediaTrack mediaTrack = allMediaTracksAfterImport.iterator().next();
        assertThat(mediaTrack.startTime, is(mediaTrackToImport.startTime));

        verify(racingEventService, never()).mediaTrackAdded(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService).mediaTrackStartTimeChanged(eq(mediaTrackToImport));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedDuration_WithOverride() throws Exception {
        String dbId = new ObjectId().toStringMongod();
        MimeType mimeType = MimeType.mp3;
        DBMediaTrack existingMediaTrack = new DBMediaTrack(dbId, "title", "url", new Date(), 0, mimeType.name());
        createRacingEventService(existingMediaTrack);

        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title, existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis + 1, mimeType);
        assertThat(existingMediaTrack.durationInMillis, is(not(mediaTrackToImport.durationInMillis)));

        mediaTracksToImport.add(mediaTrackToImport);
        racingEventService.mediaTracksImported(mediaTracksToImport, OVERRIDE);
        
        Collection<MediaTrack> allMediaTracksAfterImport = racingEventService.getAllMediaTracks();
        assertThat(allMediaTracksAfterImport.size(), is(1));
        MediaTrack mediaTrack = allMediaTracksAfterImport.iterator().next();
        assertThat(mediaTrack.durationInMillis, is(mediaTrackToImport.durationInMillis));

        verify(racingEventService, never()).mediaTrackAdded(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService).mediaTrackDurationChanged(eq(mediaTrackToImport));
    }

}
