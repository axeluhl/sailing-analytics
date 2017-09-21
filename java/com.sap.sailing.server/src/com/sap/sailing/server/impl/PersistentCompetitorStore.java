package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
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
 * Manages a persistent set of {@link Competitor}s using a {@link MongoObjectFactory} to update the persistent store,
 * and a {@link DomainObjectFactory} for initially filling this store's in-memory representation from the persistent
 * store.<p>
 * 
 * Note that the results of calling {@link CompetitorStore#allowCompetitorResetToDefaults(Competitor)} are not stored
 * persistently and therefore will be reset when re-initializing a competitor store from the database.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class PersistentCompetitorStore extends TransientCompetitorStoreImpl implements CompetitorStore {
    private static final long serialVersionUID = 9205956018421790908L;
    private transient MongoObjectFactory storeTo;
    private transient DomainObjectFactory loadFrom;
    
    /**
     * @param clear
     *            if <code>true</code>, the persistent competitor store is initially cleared, with all persistent
     *            competitor data removed; use with caution!
     */
    public PersistentCompetitorStore(MongoObjectFactory storeTo, boolean clear,
            TypeBasedServiceFinderFactory serviceFinderFactory, RaceLogResolver raceLogResolver) {
        DomainFactoryImpl baseDomainFactory = new DomainFactoryImpl(this, raceLogResolver);
        this.loadFrom = PersistenceFactory.INSTANCE.getDomainObjectFactory(MongoDBService.INSTANCE, baseDomainFactory, serviceFinderFactory);
        this.storeTo = storeTo;
        if (clear) {
            storeTo.removeAllCompetitors();
        } else {
            for (Competitor competitor : loadFrom.loadAllCompetitors()) {
                super.addNewCompetitor(competitor);
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
    
    @Override
    protected void addNewCompetitor(Competitor competitor) {
        storeTo.storeCompetitor(competitor);
        super.addNewCompetitor(competitor);
    }

    @Override
    public void clear() {
        storeTo.removeAllCompetitors();
        super.clear();
    }

    @Override
    public void removeCompetitor(Competitor competitor) {
        storeTo.removeCompetitor(competitor);
        super.removeCompetitor(competitor);
    }

    @Override
    public Competitor updateCompetitor(String idAsString, String newName, Color newRgbDisplayColor, String newEmail, String newSailId,
            Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile,
            String searchTag) {
        Competitor result = super.updateCompetitor(idAsString, newName, newRgbDisplayColor, newEmail, newSailId, newNationality,
                newTeamImageUri, newFlagImageUri, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
        storeTo.storeCompetitor(result);
        return result;
    }

    @Override
    public void addNewCompetitors(Iterable<Competitor> competitors) {
        storeTo.storeCompetitors(competitors);
        super.addNewCompetitors(competitors);
    }
}
