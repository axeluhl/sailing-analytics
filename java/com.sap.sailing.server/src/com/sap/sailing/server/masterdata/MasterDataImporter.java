package com.sap.sailing.server.masterdata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
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
        Collection<MediaTrack> tracks = topLevelMasterData.getAllMediaTracks();
        Collection<MediaTrack> existingMediaTracks = racingEventService.getAllMediaTracks();
        Map<String, MediaTrack> existingMap = new HashMap<String, MediaTrack>();

        for (MediaTrack oneTrack : existingMediaTracks) {
            existingMap.put(oneTrack.dbId, oneTrack);
        }

        for (MediaTrack oneNewTrack : tracks) {
            if (existingMap.containsKey(oneNewTrack.dbId)) {
                if (override) {
                    racingEventService.mediaTrackDeleted(existingMap.get(oneNewTrack.dbId));
                } else {
                    continue;
                }
            }
            racingEventService.mediaTrackAdded(oneNewTrack);
        }
    }

}
