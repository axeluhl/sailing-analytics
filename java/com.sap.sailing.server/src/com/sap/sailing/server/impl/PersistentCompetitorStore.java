package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.base.impl.TransientCompetitorStoreImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.mongodb.MongoDBService;

/**
 * Manages a persistent set of {@link Competitor}s and {@link Boat}s using a {@link MongoObjectFactory} to update the persistent store,
 * and a {@link DomainObjectFactory} for initially filling this store's in-memory representation from the persistent
 * store.<p>
 * 
 * Note that the results of calling {@link CompetitorStore#allowCompetitorResetToDefaults(Competitor)} and
 * {@link CompetitorStore#allowBoatResetToDefaults(Boat)} are not stored
 * persistently and therefore will be reset when re-initializing a competitor/boat store from the database.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class PersistentCompetitorStore extends TransientCompetitorStoreImpl implements CompetitorStore {
    private static final long serialVersionUID = 9205956018421790908L;
    private transient MongoObjectFactory storeTo;
    private transient DomainObjectFactory loadFrom;
    
    /**
     * @param clearCompetitorsAndBaots
     *            if <code>true</code>, the persistent competitor and boats store is initially cleared, with all persistent
     *            competitor and boat data removed; use with caution!
     */
    public PersistentCompetitorStore(MongoObjectFactory storeTo, boolean clearCompetitorsAndBaots, 
            TypeBasedServiceFinderFactory serviceFinderFactory, RaceLogResolver raceLogResolver) {
        DomainFactoryImpl baseDomainFactory = new DomainFactoryImpl(this, raceLogResolver);
        this.loadFrom = PersistenceFactory.INSTANCE.getDomainObjectFactory(MongoDBService.INSTANCE, baseDomainFactory, serviceFinderFactory);
        this.storeTo = storeTo;
        if (clearCompetitorsAndBaots) {
            storeTo.removeAllCompetitors();
            storeTo.removeAllBoats();
        } else {
            for (Competitor competitor : loadFrom.loadAllCompetitors()) {
                addCompetitorToTransientStore(competitor.getId(), competitor);
            }
            for (Boat boat: loadFrom.loadAllBoats()) {
                addBoatToTransientStore(boat.getId(), boat);
            }
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
    
    private void addCompetitorToTransientStore(Serializable id, Competitor competitor) {
        super.addNewCompetitor(id, competitor);
    }

    @Override
    protected void addNewCompetitor(Serializable id, Competitor competitor) {
        storeTo.storeCompetitor(competitor);
        super.addNewCompetitor(id, competitor);
    }

    @Override
    public void clearCompetitors() {
        storeTo.removeAllCompetitors();
        super.clearCompetitors();
    }

    @Override
    public void removeCompetitor(Competitor competitor) {
        storeTo.removeCompetitor(competitor);
        super.removeCompetitor(competitor);
    }

    @Override
    public Competitor updateCompetitor(String idAsString, String newName, String newShortName, Color newRgbDisplayColor, String newEmail, String newSailId,
            Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile,
            String searchTag) {
        Competitor result = super.updateCompetitor(idAsString, newName, newShortName, newRgbDisplayColor, newEmail, newSailId, newNationality,
                newTeamImageUri, newFlagImageUri, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
        storeTo.storeCompetitor(result);
        return result;
    }
    
    private void addBoatToTransientStore(Serializable id, Boat boat) {
        super.addNewBoat(id, boat);
    }

    @Override
    protected void addNewBoat(Serializable id, Boat boat) {
        storeTo.storeBoat(boat);
        super.addNewBoat(id, boat);
    }

    @Override
    public void clearBoats() {
        storeTo.removeAllBoats();
        super.clearBoats();
    }

    @Override
    public void removeBoat(Boat boat) {
        storeTo.removeBoat(boat);
        super.removeBoat(boat);
    }

    @Override
    public Boat updateBoat(String idAsString, String newName, Color newColor, String newSailId) {
        Boat result = super.updateBoat(idAsString, newName, newColor, newSailId);
        storeTo.storeBoat(result);
        return result;
    }
}
