package com.sap.sailing.server.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.impl.TransientCompetitorStoreImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;

/**
 * Manages a persistent set of {@link Competitor}s using a {@link MongoObjectFactory} to update the persistent store,
 * and a {@link DomainObjectFactory} for initially filling this store's in-memory representation from the persistent
 * store.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class PersistentCompetitorStore extends TransientCompetitorStoreImpl implements CompetitorStore {
    private final MongoObjectFactory storeTo;
    
    public PersistentCompetitorStore(DomainObjectFactory loadFrom, MongoObjectFactory storeTo) {
        this.storeTo = storeTo;
        for (Competitor competitor : loadFrom.loadAllCompetitors()) {
            addCompetitorToTransientStore(competitor.getId(), competitor);
        }
    }
    
    private void addCompetitorToTransientStore(Serializable id, Competitor competitor) {
        super.addNewCompetitor(id, competitor);
    }

    @Override
    protected void addNewCompetitor(Serializable id, Competitor competitor) {
        storeTo.storeCompetitor(competitor);
        super.addNewCompetitor(id, competitor);
    }
}
