package com.sap.sailing.server.masterdata;

import java.util.UUID;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
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
        racingEventService.mediaTracksImported(topLevelMasterData.getAllMediaTracks(), override);

        return creationCount;
    }

}
