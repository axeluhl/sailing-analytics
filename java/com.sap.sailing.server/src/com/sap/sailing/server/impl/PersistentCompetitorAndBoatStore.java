package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorWithBoatImpl;
import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.base.impl.TransientCompetitorAndBoatStoreImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.mongodb.MongoDBService;

/**
 * Manages a persistent set of {@link Competitor}s, {@link CompetitorWithBoat}s and {@link Boat}s using a {@link MongoObjectFactory} to update the persistent store,
 * and a {@link DomainObjectFactory} for initially filling this store's in-memory representation from the persistent
 * store.<p>
 * 
 * Note that the results of calling {@link CompetitorAndBoatStore#allowCompetitorResetToDefaults(Competitor)} and
 * {@link CompetitorAndBoatStore#allowBoatResetToDefaults(Boat)} are not stored
 * persistently and therefore will be reset when re-initializing a competitor/boat store from the database.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class PersistentCompetitorAndBoatStore extends TransientCompetitorAndBoatStoreImpl implements CompetitorAndBoatStore {
    private static final long serialVersionUID = 9205956018421790908L;
    private transient MongoObjectFactory storeTo;
    private transient DomainObjectFactory loadFrom;
    private static final Logger logger = Logger.getLogger(PersistentCompetitorAndBoatStore.class.getName());
    
    /**
     * @param clearCompetitorsAndBoats
     *            if <code>true</code>, the persistent competitor and boats store is initially cleared, with all persistent
     *            competitor and boat data removed; use with caution!
     */
    public PersistentCompetitorAndBoatStore(MongoObjectFactory storeTo, boolean clearCompetitorsAndBoats, 
            TypeBasedServiceFinderFactory serviceFinderFactory, RaceLogResolver raceLogResolver) {
        DomainFactoryImpl baseDomainFactory = new DomainFactoryImpl(this, raceLogResolver);
        this.loadFrom = PersistenceFactory.INSTANCE.getDomainObjectFactory(MongoDBService.INSTANCE, baseDomainFactory, serviceFinderFactory);
        this.storeTo = storeTo;
        migrateCompetitorsIfRequired();
        if (clearCompetitorsAndBoats) {
            storeTo.removeAllBoats();
            storeTo.removeAllCompetitors();
        } else {
            Collection<DynamicBoat> allBoats = loadFrom.loadAllBoats();
            for (DynamicBoat boat : allBoats) {
                super.addNewBoat(boat);
            }
            Collection<DynamicCompetitor> allCompetitors = loadFrom.loadAllCompetitors();
            for (DynamicCompetitor competitor : allCompetitors) {
                super.addNewCompetitor(competitor);
            }
        }
    }
    
    /**
     * Migrate competitors with contained boats (before bug2822) to competitors with separate boats if required
     */
    private void migrateCompetitorsIfRequired() {
        Iterable<CompetitorWithBoat> allLegacyCompetitorsWithBoat = loadFrom.migrateLegacyCompetitorsIfRequired();
        if (allLegacyCompetitorsWithBoat != null) {
            List<CompetitorWithBoat> newCompetitors = new ArrayList<>();
            List<Boat> newBoats = new ArrayList<>();
            for (CompetitorWithBoat competitorWithBoat : allLegacyCompetitorsWithBoat) {
                Boat containedBoat = competitorWithBoat.getBoat();
                // Create a new random uuid for the boats to make sure the competitor uuid is not used  
                UUID boatUUID = UUID.randomUUID();
                DynamicBoat newBoat = new BoatImpl(boatUUID, null /* the old boat names were nonsense names */, containedBoat.getBoatClass(), containedBoat.getSailID());
                newBoats.add(newBoat);
                CompetitorWithBoat newCompetitorWithBoat = new CompetitorWithBoatImpl((Competitor) competitorWithBoat, newBoat);
                newCompetitors.add(newCompetitorWithBoat);
            }
            logger.log(Level.INFO, "Bug2822 DB-Migration: Store migrated competitors and boats.");
            storeTo.storeCompetitors(newCompetitors);
            storeTo.storeBoats(newBoats);
        }
    }

    DomainObjectFactory getDomainObjectFactory() {
        return loadFrom;
    }
    
    MongoObjectFactory getMongoObjectFactory() {
        return storeTo;
    }
    
    DomainFactory getBaseDomainFactory() {
        return loadFrom.getBaseDomainFactory();
    }
    
    /**
     * Uses the {@link PersistenceFactory#getDefaultMongoObjectFactory() default DB persistence} to store new competitors.
     * Use {@link #setStoreTo} to change this after de-serialization.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        storeTo = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory();
    }

    @Override
    public synchronized Competitor migrateToCompetitorWithoutBoat(CompetitorWithBoat competitorWithBoat) {
        // It should not be possible to call this for a competitor which has already been migrated.
        assert competitorWithBoat.hasBoat();
        logger.log(Level.INFO, "Bug2822 DB-Migration: Migrate competitor " + competitorWithBoat.getName() + " with ID " + competitorWithBoat.getId().toString() + " to have a separate boat"); 
        Competitor migratedCompetitor = super.migrateToCompetitorWithoutBoat(competitorWithBoat);
        storeTo.storeCompetitor(migratedCompetitor);
        return migratedCompetitor; 
    }

    @Override
    protected void addNewCompetitor(DynamicCompetitor competitor) {
        storeTo.storeCompetitor(competitor);
        super.addNewCompetitor(competitor);
    }

    @Override
    public void clearCompetitors() {
        storeTo.removeAllCompetitors();
        super.clearCompetitors();
    }

    protected void removeCompetitor(Competitor competitor) {
        storeTo.removeCompetitor(competitor);
        super.removeCompetitor(competitor);
    }

    @Override
    public Competitor updateCompetitor(String idAsString, String newName, String newShortName, Color newRgbDisplayColor, String newEmail,
            Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile,
            String searchTag) {
        Competitor result = super.updateCompetitor(idAsString, newName, newShortName, newRgbDisplayColor, newEmail, newNationality,
                newTeamImageUri, newFlagImageUri, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
        storeTo.storeCompetitor(result);
        return result;
    }
    
    @Override
    public void addNewCompetitors(Iterable<DynamicCompetitor> competitors) {
        storeTo.storeCompetitors(competitors);
        super.addNewCompetitors(competitors);
    }

    @Override
    protected void addNewBoat(DynamicBoat boat) {
        storeTo.storeBoat(boat);
        super.addNewBoat(boat);
    }

    @Override
    public void clearBoats() {
        storeTo.removeAllBoats();
        super.clearBoats();
    }

    protected void removeBoat(Boat boat) {
        storeTo.removeBoat(boat);
        super.removeBoat(boat);
    }

    @Override
    public Boat updateBoat(String idAsString, String newName, Color newColor, String newSailId) {
        Boat result = super.updateBoat(idAsString, newName, newColor, newSailId);
        storeTo.storeBoat(result);
        return result;
    }
    
    @Override
    public void addNewBoats(Iterable<DynamicBoat> boats) {
        storeTo.storeBoats(boats);
        super.addNewBoats(boats);
    }
}
