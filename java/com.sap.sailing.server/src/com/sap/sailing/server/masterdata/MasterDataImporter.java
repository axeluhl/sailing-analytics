package com.sap.sailing.server.masterdata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.operationaltransformation.ImportMasterDataOperation;

public class MasterDataImporter {
    private final DomainFactory baseDomainFactory;
    
    private final RacingEventService racingEventService;
    
    public MasterDataImporter(DomainFactory baseDomainFactory, RacingEventService racingEventService) {
        this.baseDomainFactory = baseDomainFactory;
        this.racingEventService = racingEventService;
    }

    public MasterDataImportObjectCreationCount importMasterData(TopLevelMasterData topLevelMasterData,
            UUID importOperationId, boolean override) {
        MasterDataImportObjectCreationCountImpl creationCount = new MasterDataImportObjectCreationCountImpl();
        ImportMasterDataOperation op = new ImportMasterDataOperation(topLevelMasterData, importOperationId, override,
                creationCount,
                baseDomainFactory);
        creationCount = racingEventService.apply(op);
        createMediaTracks(topLevelMasterData, override);

        return creationCount;
    }

    private void createMediaTracks(TopLevelMasterData topLevelMasterData, boolean override) {
        Collection<MediaTrack> tracksToImport = topLevelMasterData.getAllMediaTracks();
        Collection<MediaTrack> existingMediaTracks = racingEventService.getAllMediaTracks();
        Map<String, MediaTrack> existingMap = new HashMap<String, MediaTrack>();

        for (MediaTrack existingTrack : existingMediaTracks) {
            existingMap.put(existingTrack.dbId, existingTrack);
        }

        for (MediaTrack trackToImport : tracksToImport) {
            MediaTrack existingTrack = existingMap.get(trackToImport.dbId);
            if (existingTrack == null) {
                racingEventService.mediaTrackAdded(trackToImport);
            } else {
                if (override) {
                    
                    // Using fine-grained update methods.
                    // Rationale: Changes on more than one track property are rare 
                    //            and don't justify the introduction of a new set 
                    //            of methods (including replication).
                    if (!Util.equalsWithNull(existingTrack.title, trackToImport.title)) {
                        racingEventService.mediaTrackTitleChanged(trackToImport);
                    }
                    if (!Util.equalsWithNull(existingTrack.url, trackToImport.url)) {
                        racingEventService.mediaTrackUrlChanged(trackToImport);
                    }
                    if (!Util.equalsWithNull(existingTrack.startTime, trackToImport.startTime)) {
                        racingEventService.mediaTrackStartTimeChanged(trackToImport);
                    }
                    if (existingTrack.durationInMillis != trackToImport.durationInMillis) {
                        racingEventService.mediaTrackDurationChanged(trackToImport);
                    }
                }
            }
        }
    }

}
