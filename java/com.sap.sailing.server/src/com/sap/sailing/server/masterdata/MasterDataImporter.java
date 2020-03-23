package com.sap.sailing.server.masterdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
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
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.operationaltransformation.ImportMasterDataOperation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache.ResolveListener;

public class MasterDataImporter {
    private final static Logger logger = Logger.getLogger(MasterDataImporter.class.getName());
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
            throws IOException, ClassNotFoundException, Exception {
        ObjectInputStreamResolvingAgainstCache<DomainFactory> objectInputStream = racingEventService
                .getBaseDomainFactory()
                .createObjectInputStreamResolvingAgainstThisFactory(inputStream, new ResolveListener() {
                    @Override
                    public void onNewObject(Object result) {
                        if (result instanceof Boat || result instanceof Competitor) {
                            QualifiedObjectIdentifier id = ((WithQualifiedObjectIdentifier) result).getIdentifier();
                            logger.info("Adopting " + id + " from Masterdataimport  to " + user.getName() + " and group "
                                    + (tenant==null ? "null" : tenant.getName()));
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

            // master data import from older system, generate a uuid for this.
            if (regatta.getRegistrationLinkSecret() == null) {
                logger.info("Generated missing registrationLinkSecret for " + this + " while importing MasterData");
                regatta.setRegistrationLinkSecret(UUID.randomUUID().toString());
            }
        }
        
        // RaceTrackingConnectivityParameters de-serialization
//        Thread.currentThread().setContextClassLoader(racingEventService.getCombinedMasterDataClassLoader());
////        MongoObjectFactory mongoObjectFactory = racingEventService.getMongoObjectFactory();
////        DomainObjectFactory domainObjectFactory = racingEventService.getDomainObjectFactory();
////        TypeBasedServiceFinder<RaceTrackingConnectivityParametersHandler> serviceFinder = domainObjectFactory.getRaceTrackingConnectivityParamsServiceFinder();        
////        WindStore windStore = MongoWindStoreFactory.INSTANCE.getMongoWindStore(mongoObjectFactory, domainObjectFactory);
//        ArrayList<RaceTrackingConnectivityParameters> connectivityParametersToRestore = (ArrayList<RaceTrackingConnectivityParameters>) objectInputStream.readObject();        
////        for (RaceTrackingConnectivityParameters param : connectivityParametersToRestore) {
////            final String typeIdentifier = param.getTypeIdentifier();
////            final RaceTrackingConnectivityParametersHandler handler = serviceFinder.findService(typeIdentifier);
////            final PermissionAwareRaceTrackingHandler raceTrackingHandler = new PermissionAwareRaceTrackingHandler(racingEventService.getSecurityService());
////            switch (typeIdentifier) {
////            case "TRAC_TRAC":
////                assert param instanceof RaceTrackingConnectivityParametersImpl;
////                final RaceTrackingConnectivityParametersImpl ttParam = (RaceTrackingConnectivityParametersImpl) handler.resolve(param);
////                // racingEventService.addRace(/* addToRegatta==null means "default regatta" */ null, ttParam,
////                // ttParam.getTimeoutInMillis(), raceTrackingHandler);
////                // ttParam.createRaceTracker(racingEventService, windStore, racingEventService, racingEventService,
////                // ttParam.getTimeoutInMillis(), raceTrackingHandler);
////                connectivityParametersToRestore.add(ttParam);
////                break;
////            }
//////            case "RACE_LOG_TRACKING":
//////                assert param instanceof RaceLogConnectivityParams;
//////                final RaceLogConnectivityParams rLParam = (RaceLogConnectivityParams) handler.resolve(param);
//////                break;
//////            }
////            connectivityParametersToRestore.remove(param);
////        }
//        Thread.currentThread().setContextClassLoader(oldContextClassLoader);

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
                creationCount, user, tenant);
        creationCount = racingEventService.apply(op);
        return creationCount;
    }

}
