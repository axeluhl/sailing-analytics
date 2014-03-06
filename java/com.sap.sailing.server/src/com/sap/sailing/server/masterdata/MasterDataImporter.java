package com.sap.sailing.server.masterdata;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.ObjectInputStreamResolvingAgainstDomainFactory;
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

    private MasterDataImportObjectCreationCount applyMasterDataImportOperation(TopLevelMasterData topLevelMasterData,
            UUID importOperationId, boolean override) {
        MasterDataImportObjectCreationCountImpl creationCount = new MasterDataImportObjectCreationCountImpl();
        ImportMasterDataOperation op = new ImportMasterDataOperation(topLevelMasterData, importOperationId, override,
                creationCount,
                baseDomainFactory);
        creationCount = racingEventService.apply(op);
        racingEventService.replicateDataImportOperation(op);
        racingEventService.mediaTracksImported(topLevelMasterData.getAllMediaTracks(), override);

        return creationCount;
    }

    public void importFromStream(InputStream inputStream, UUID importOperationId, boolean override) throws IOException,
            ClassNotFoundException {
        ObjectInputStreamResolvingAgainstDomainFactory objectInputStream = racingEventService.getBaseDomainFactory()
                .createObjectInputStreamResolvingAgainstThisFactory(inputStream);
        racingEventService
                .createOrUpdateDataImportProgressWithReplication(importOperationId, 0.03, "Reading Data", 0.5);
        TopLevelMasterData topLevelMasterData = (TopLevelMasterData) objectInputStream.readObject();

        racingEventService.createOrUpdateDataImportProgressWithReplication(importOperationId, 0.3,
                "Data-Transfer Complete, Initializing Import Operation", 0.5);

        applyMasterDataImportOperation(topLevelMasterData, importOperationId, override);
    }

}
