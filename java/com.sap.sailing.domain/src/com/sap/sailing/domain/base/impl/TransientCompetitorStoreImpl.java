package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.Nationality;

public class TransientCompetitorStoreImpl implements CompetitorStore, Serializable {
    private static final long serialVersionUID = -4198298775476586931L;
    private final ConcurrentHashMap<Serializable, Competitor> competitorCache;
    private final ConcurrentHashMap<String, Competitor> competitorsByIdAsString;
    
    public TransientCompetitorStoreImpl() {
        competitorCache = new ConcurrentHashMap<>();
        competitorsByIdAsString = new ConcurrentHashMap<>();
    }
    
    private Competitor createCompetitor(Serializable id, String name, DynamicTeam team, DynamicBoat boat) {
        Competitor result = new CompetitorImpl(id, name, team, boat);
        addNewCompetitor(id, result);
        return result;
    }

    /**
     * Adds the <code>competitor</code> to this transient competitor collection so that it is available in
     * {@link #getExistingCompetitorById(Serializable)}. Subclasses may override in case they need to take additional
     * measures such as durably storing the competitor. Overriding implementations must call this implementation.
     */
    protected void addNewCompetitor(Serializable id, Competitor competitor) {
        competitorCache.put(id, competitor);
        competitorsByIdAsString.put(id.toString(), competitor);
    }
    
    @Override
    public Competitor getOrCreateCompetitor(Serializable competitorId, String name, DynamicTeam team, DynamicBoat boat) {
        Competitor result = getExistingCompetitorById(competitorId); // avoid synchronization for successful read access
        if (result == null) {
            synchronized(this) {
                result = getExistingCompetitorById(competitorId); //  try again, now with synchronization
                if (result == null) {
                    result = createCompetitor(competitorId, name, team, boat);
                }
            }
        }
        return result;
    }

    @Override
    public Competitor getExistingCompetitorById(Serializable competitorId) {
        return competitorCache.get(competitorId);
    }

    @Override
    public Competitor getExistingCompetitorByIdAsString(String competitorIdAsString) {
        return competitorsByIdAsString.get(competitorIdAsString);
    }

    @Override
    public int size() {
        return competitorCache.size();
    }

    @Override
    public void clear() {
        competitorCache.clear();
    }
    
    @Override
    public Iterable<Competitor> getCompetitors() {
        return Collections.unmodifiableCollection(competitorCache.values());
    }

    @Override
    public void removeCompetitor(Competitor competitor) {
        competitorCache.remove(competitor.getId());
        competitorsByIdAsString.remove(competitor.getId().toString());  
    }

    @Override
    public Competitor updateCompetitor(Serializable id, String newName, String newSailId, Nationality newNationality) {
        DynamicCompetitor competitor = (DynamicCompetitor) getExistingCompetitorById(id);
        if (competitor != null) {
            competitor.setName(newName);
            competitor.getBoat().setSailId(newSailId);
            competitor.getTeam().setNationality(newNationality);
        }
        return competitor;
    }
    
}
