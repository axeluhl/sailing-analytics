package com.sap.sailing.server.masterdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.MasterDataImportInformation;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.DataImportSubProgress;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLogStore;
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

    public Map<LeaderboardGroup, ? extends Iterable<Event>> importFromStream(InputStream inputStream, UUID importOperationId, boolean override)
            throws IOException, ClassNotFoundException {
        ObjectInputStreamResolvingAgainstCache<DomainFactory> objectInputStream = racingEventService
                .getBaseDomainFactory()
                .createObjectInputStreamResolvingAgainstThisFactory(inputStream, new ResolveListener() {
                    @Override
                    public void onNewObject(Object result) {
                        if (result instanceof Boat || result instanceof Competitor) {
                            QualifiedObjectIdentifier id = ((WithQualifiedObjectIdentifier) result).getIdentifier();
                            logger.info("Adopting " + id + " from Masterdataimport to " + user.getName() + " and group "
                                    + (tenant==null ? "null" : tenant.getName()));
                            racingEventService.getSecurityService().setOwnershipIfNotSet(id, user, tenant);
                        }
                    }

                    @Override
                    public void onResolvedObject(Object result) {
                    }
                }, /* classLoaderCache */ new HashMap<>());
        racingEventService.createOrUpdateDataImportProgressWithReplication(importOperationId, 0.03,
                DataImportSubProgress.TRANSFER_STARTED, 0.5);
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(
                racingEventService.getMongoObjectFactory(), racingEventService.getDomainObjectFactory());
        RegattaImpl.setOngoingMasterDataImport(new MasterDataImportInformation(raceLogStore));
        final ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        final TopLevelMasterData topLevelMasterData;
        Thread.currentThread().setContextClassLoader(racingEventService.getDeserializationClassLoader());
        try {
            @SuppressWarnings("unchecked")
            final List<Serializable> competitorIds = (List<Serializable>) objectInputStream.readObject();
            if (override) {
                setAllowCompetitorsDataToBeReset(competitorIds);
            }
            // Deserialize Regattas to make sure that Regattas are deserialized before Series
            objectInputStream.readObject();
            topLevelMasterData = (TopLevelMasterData) objectInputStream.readObject();
        } finally {
            RegattaImpl.setOngoingMasterDataImport(null);
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
        // in order to restore all listeners we need to initialize the regatta
        // after the whole object graph has been restored
        for (Regatta regatta : topLevelMasterData.getAllRegattas()) {
            RegattaImpl regattaImpl = (RegattaImpl) regatta;
            regattaImpl.initializeSeriesAfterDeserialize();
            // master data import from older system, generate a uuid for this.
            if (regatta.getRegistrationLinkSecret() == null) {
                logger.info("Generated missing registrationLinkSecret for " + this + " while importing MasterData");
                regatta.setRegistrationLinkSecret(UUID.randomUUID().toString());
            }
        }
        racingEventService.createOrUpdateDataImportProgressWithReplication(importOperationId, 0.3,
                DataImportSubProgress.TRANSFER_COMPLETED, 0.5);
        applyMasterDataImportOperation(topLevelMasterData, importOperationId, override);
        return topLevelMasterData.getEventForLeaderboardGroup();
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

    /**
     * Replicates a stripped-down version of the {@code topLevelMasterData} to any replica attached. We assume here that
     * the {@link #racingEventService} provided to this imported is the "master" instance of this service. This must be
     * guaranteed by any service invoking this method, be it a REST API or a GWT RPC; they all need to ensure that their
     * request has previously been routed to the master node for the {@link RacingEventService}. The reason for this is
     * that the {@link TopLevelMasterData} object used for replication will have all tracking data stripped off which
     * helps reducing the object size to make it very likely for the operation to fit into a RabbitMQ replication
     * message, and because transmitting the tracking data to a replica this way would be redundant because it will get
     * replicated as soon as the master node starts loading those races.
     * 
     * @param topLevelMasterData
     *            the full master data with all tracking data attached; for replication, a stripped-down
     *            {@link TopLevelMasterData#copyAndStripOffDataNotNeededOnReplicas() copy} will be created. The
     *            full version will be applied to the {@link #racingEventService} locally.
     */
    private MasterDataImportObjectCreationCount applyMasterDataImportOperation(TopLevelMasterData topLevelMasterData,
            UUID importOperationId, boolean override) {
        ImportMasterDataOperation strippedOpForReplicas = new ImportMasterDataOperation(
                topLevelMasterData.copyAndStripOffDataNotNeededOnReplicas(), importOperationId, override, user,
                tenant);
        // replicate explicitly first and let isRequiresExplicitTransitiveReplication return false; see also bug5574
        racingEventService.replicate(strippedOpForReplicas);
        ImportMasterDataOperation op = new ImportMasterDataOperation(topLevelMasterData, importOperationId, override,
                user, tenant);
        return racingEventService.apply(op);
    }

}
