package com.sap.sailing.server.masterdata;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.server.RacingEventService;

public class MasterDataImporterMediaTest {
    
    private static final boolean NO_OVERRIDE = false;
    private static final boolean OVERRIDE = true;

    private MasterDataImporter importer;
    private RacingEventService racingEventService;
    private DomainFactory baseDomainFactory = mock(DomainFactory.class);
    private Set<LeaderboardGroup> groupsToExport = Collections.emptySet();
    private Iterable<Event> allEvents = Collections.emptyList();
    private Map<String, Regatta> regattaForRaceIdString = Collections.emptyMap();
    private Collection<MediaTrack> existingMediaTracks;
    private TopLevelMasterData topLevelMasterData ;
    
    @Before
    public void before() {
        existingMediaTracks = new ArrayList<MediaTrack>();
        racingEventService = mock(RacingEventService.class);
        when(racingEventService.getAllMediaTracks()).thenReturn(existingMediaTracks);
        importer = new MasterDataImporter(baseDomainFactory, racingEventService);
    }

    private TopLevelMasterData createMasterData(MediaTrack... mediaTracksToImport) {
        return topLevelMasterData = new TopLevelMasterData(groupsToExport, allEvents, regattaForRaceIdString, Arrays.asList(mediaTracksToImport));
    }
    
    @Test
    public void testEmptyImportList_NoOverride() throws Exception {
        createMasterData();
        MasterDataImportObjectCreationCount importResult = importer.importMasterData(topLevelMasterData,
                UUID.randomUUID(), NO_OVERRIDE);
        assertNull(importResult); //according to what the mocked racing event service returns
    }

    @Test
    public void testEmptyImportList_WithOverride() throws Exception {
        createMasterData();
        MasterDataImportObjectCreationCount importResult = importer.importMasterData(topLevelMasterData,
                UUID.randomUUID(), OVERRIDE);
        assertNull(importResult); //according to what the mocked racing event service returns
    }

    @Test
    public void testImportOneTrackToEmptyTarget_WithOverride() throws Exception {
        MediaTrack mediaTrackToImport = new MediaTrack("dbId", "title", "url", new Date(), 0, MimeType.mp3);
        createMasterData(mediaTrackToImport);
        importer.importMasterData(topLevelMasterData, UUID.randomUUID(), OVERRIDE);
        verify(racingEventService).mediaTrackAdded(mediaTrackToImport);
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
 
    }

    @Test
    public void testImportOneTrackToSameTarget_WithOverride() throws Exception {
        MediaTrack existingMediaTrack = new MediaTrack("dbId", "title", "url", new Date(), 0, MimeType.mp3);
        existingMediaTracks.add(existingMediaTrack);
        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title, existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, existingMediaTrack.mimeType);
        createMasterData(mediaTrackToImport);
        importer.importMasterData(topLevelMasterData, UUID.randomUUID(), OVERRIDE);
        verify(racingEventService, never()).mediaTrackAdded(mediaTrackToImport);
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
 
    }

    @Test
    public void testImportOneTrackToExistingOtherTrack_WithOverride() throws Exception {
        MediaTrack existingMediaTrack = new MediaTrack("dbId", "title", "url", new Date(), 0, MimeType.mp3);
        existingMediaTracks.add(existingMediaTrack);
        MediaTrack mediaTrackToImport = new MediaTrack("dbId2", existingMediaTrack.title, existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, existingMediaTrack.mimeType);
        createMasterData(mediaTrackToImport);
        importer.importMasterData(topLevelMasterData, UUID.randomUUID(), OVERRIDE);
        verify(racingEventService).mediaTrackAdded(mediaTrackToImport);
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
 
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedTitle_WithOverride() throws Exception {
        MediaTrack existingMediaTrack = new MediaTrack("dbId", "title", "url", new Date(), 0, MimeType.mp3);
        existingMediaTracks.add(existingMediaTrack);
        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title + "x", existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, existingMediaTrack.mimeType);
        createMasterData(mediaTrackToImport);
        importer.importMasterData(topLevelMasterData, UUID.randomUUID(), OVERRIDE);
        verify(racingEventService, never()).mediaTrackAdded(mediaTrackToImport);
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
 
    }

    @Test
    public void testImportOneTrackWithNullTitleToSameTargetWithTitle_WithOverride() throws Exception {
        MediaTrack existingMediaTrack = new MediaTrack("dbId", "title", "url", new Date(), 0, MimeType.mp3);
        existingMediaTracks.add(existingMediaTrack);
        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, null, existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, existingMediaTrack.mimeType);
        createMasterData(mediaTrackToImport);
        importer.importMasterData(topLevelMasterData, UUID.randomUUID(), OVERRIDE);
        verify(racingEventService, never()).mediaTrackAdded(mediaTrackToImport);
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
 
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedTitle_NoOverride() throws Exception {
        MediaTrack existingMediaTrack = new MediaTrack("dbId", "title", "url", new Date(), 0, MimeType.mp3);
        existingMediaTracks.add(existingMediaTrack);
        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title + "x", existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis, existingMediaTrack.mimeType);
        createMasterData(mediaTrackToImport);
        importer.importMasterData(topLevelMasterData, UUID.randomUUID(), NO_OVERRIDE);
        verify(racingEventService, never()).mediaTrackAdded(mediaTrackToImport);
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
 
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedUrl_WithOverride() throws Exception {
        MediaTrack existingMediaTrack = new MediaTrack("dbId", "title", "url", new Date(), 0, MimeType.mp3);
        existingMediaTracks.add(existingMediaTrack);
        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title, existingMediaTrack.url + "x", existingMediaTrack.startTime, existingMediaTrack.durationInMillis, existingMediaTrack.mimeType);
        createMasterData(mediaTrackToImport);
        importer.importMasterData(topLevelMasterData, UUID.randomUUID(), OVERRIDE);
        verify(racingEventService, never()).mediaTrackAdded(mediaTrackToImport);
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
 
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedStarttime_WithOverride() throws Exception {
        MediaTrack existingMediaTrack = new MediaTrack("dbId", "title", "url", new Date(), 0, MimeType.mp3);
        existingMediaTracks.add(existingMediaTrack);
        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title, existingMediaTrack.url, new Date(existingMediaTrack.startTime.getTime() + 1), existingMediaTrack.durationInMillis, existingMediaTrack.mimeType);
        createMasterData(mediaTrackToImport);
        importer.importMasterData(topLevelMasterData, UUID.randomUUID(), OVERRIDE);
        verify(racingEventService, never()).mediaTrackAdded(mediaTrackToImport);
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackDurationChanged(any(MediaTrack.class));
 
    }

    @Test
    public void testImportOneTrackToSameTargetWithChangedDuration_WithOverride() throws Exception {
        MediaTrack existingMediaTrack = new MediaTrack("dbId", "title", "url", new Date(), 0, MimeType.mp3);
        existingMediaTracks.add(existingMediaTrack);
        MediaTrack mediaTrackToImport = new MediaTrack(existingMediaTrack.dbId, existingMediaTrack.title, existingMediaTrack.url, existingMediaTrack.startTime, existingMediaTrack.durationInMillis + 1, existingMediaTrack.mimeType);
        createMasterData(mediaTrackToImport);
        importer.importMasterData(topLevelMasterData, UUID.randomUUID(), OVERRIDE);
        verify(racingEventService, never()).mediaTrackAdded(mediaTrackToImport);
        verify(racingEventService, never()).mediaTrackDeleted(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackTitleChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackUrlChanged(any(MediaTrack.class));
        verify(racingEventService, never()).mediaTrackStartTimeChanged(any(MediaTrack.class));
        verify(racingEventService).mediaTrackDurationChanged(any(MediaTrack.class));
 
    }

}
