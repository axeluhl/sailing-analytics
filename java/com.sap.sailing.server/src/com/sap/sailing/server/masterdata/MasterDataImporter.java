package com.sap.sailing.server.masterdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.MasterDataImportInformation;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.DataImportSubProgress;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.operationaltransformation.ImportMasterDataOperation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache.ResolveListener;

public class MasterDataImporter {
    private final static Logger LOG = Logger.getLogger(MasterDataImporter.class.getName());
    private final DomainFactory baseDomainFactory;
    private final RacingEventService racingEventService;
    private final User user;
    private final UserGroup tenant;

    public MasterDataImporter(DomainFactory baseDomainFactory, RacingEventService racingEventService,
            User user, UserGroup tenant) {
        this.baseDomainFactory = baseDomainFactory;
        this.racingEventService = racingEventService;
        this.user = user;
        this.tenant = tenant;

    }

    public void importFromStream(InputStream inputStream, UUID importOperationId, boolean override)
            throws IOException,
            ClassNotFoundException {
        ObjectInputStreamResolvingAgainstCache<DomainFactory> objectInputStream = racingEventService
                .getBaseDomainFactory()
                .createObjectInputStreamResolvingAgainstThisFactory(inputStream, new ResolveListener() {
                    @Override
                    public void onNewObject(Object result) {
                        if (result instanceof WithQualifiedObjectIdentifier) {
                            QualifiedObjectIdentifier id = ((WithQualifiedObjectIdentifier) result).getIdentifier();
                            LOG.info("Adopting " + id + " from Masterdataimport");
                            racingEventService.getSecurityService().setOwnershipIfNotSet(id, user, tenant);
                        }
                    }

                    @Override
                    public void onResolvedObject(Object result) {
                    }
                });
        racingEventService.createOrUpdateDataImportProgressWithReplication(importOperationId, 0.03,
                DataImportSubProgress.TRANSFER_STARTED, 0.5);
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(
                racingEventService.getMongoObjectFactory(), racingEventService.getDomainObjectFactory());
        RegattaImpl.setOngoingMasterDataImport(new MasterDataImportInformation(raceLogStore));
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(racingEventService.getCombinedMasterDataClassLoader());
        @SuppressWarnings("unchecked")
        final List<Serializable> competitorIds = (List<Serializable>) objectInputStream.readObject();
        if (override) {
            setAllowCompetitorsDataToBeReset(competitorIds);
        }
        // Deserialize Regattas to make sure that Regattas are deserialized before Series
        objectInputStream.readObject();
        TopLevelMasterData topLevelMasterData = (TopLevelMasterData) objectInputStream.readObject();
        RegattaImpl.setOngoingMasterDataImport(null);
        Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        // in order to restore all listeners we need to initialize the regatta
        // after the whole object graph has been restored
        for (Regatta regatta : topLevelMasterData.getAllRegattas()) {
            RegattaImpl regattaImpl = (RegattaImpl)regatta;
            regattaImpl.initializeSeriesAfterDeserialize();
        }
        racingEventService.createOrUpdateDataImportProgressWithReplication(importOperationId, 0.3,
                DataImportSubProgress.TRANSFER_COMPLETED, 0.5);
        applyMasterDataImportOperation(topLevelMasterData, importOperationId, override);
    }

    private void setAllowCompetitorsDataToBeReset(List<Serializable> competitorIds) {
        CompetitorAndBoatStore store = baseDomainFactory.getCompetitorAndBoatStore();
        for (Serializable id : competitorIds) {
            Competitor competitor = baseDomainFactory.getExistingCompetitorById(id);
            if (competitor != null) {
                store.allowCompetitorResetToDefaults(competitor);
            }
        }
    }

    private MasterDataImportObjectCreationCount applyMasterDataImportOperation(TopLevelMasterData topLevelMasterData,
            UUID importOperationId, boolean override) {
        MasterDataImportObjectCreationCountImpl creationCount = new MasterDataImportObjectCreationCountImpl();
        ImportMasterDataOperation op = new ImportMasterDataOperation(topLevelMasterData, importOperationId, override,
                creationCount);
        creationCount = racingEventService.apply(op);
        return creationCount;
    }

}
