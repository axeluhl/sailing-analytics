package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

public class TransientCompetitorStoreImpl implements CompetitorStore, Serializable {
    private static final long serialVersionUID = -4198298775476586931L;
    private final ConcurrentHashMap<Serializable, Competitor> competitorCache;
    private final ConcurrentHashMap<String, Competitor> competitorsByIdAsString;
    private final WeakHashMap<Competitor, CompetitorDTO> weakCompetitorDTOCache;
    
    private final NamedReentrantReadWriteLock lock;

    public TransientCompetitorStoreImpl() {
        lock = new NamedReentrantReadWriteLock("CompetitorStore", /* fair */ false);
        competitorCache = new ConcurrentHashMap<>();
        competitorsByIdAsString = new ConcurrentHashMap<>();
        weakCompetitorDTOCache = new WeakHashMap<>();
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
        LockUtil.lockForWrite(lock);
        try {
            competitorCache.put(id, competitor);
            competitorsByIdAsString.put(id.toString(), competitor);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }
    
    @Override
    public Competitor getOrCreateCompetitor(Serializable competitorId, String name, DynamicTeam team, DynamicBoat boat) {
        Competitor result = getExistingCompetitorById(competitorId); // avoid synchronization for successful read access
        if (result == null) {
            LockUtil.lockForWrite(lock);
            try {
                result = getExistingCompetitorById(competitorId); // try again, now while holding the write lock
                if (result == null) {
                    result = createCompetitor(competitorId, name, team, boat);
                }
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        }
        return result;
    }

    @Override
    public Competitor getExistingCompetitorById(Serializable competitorId) {
        LockUtil.lockForRead(lock);
        try {
            return competitorCache.get(competitorId);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public Competitor getExistingCompetitorByIdAsString(String competitorIdAsString) {
        LockUtil.lockForRead(lock);
        try {
            return competitorsByIdAsString.get(competitorIdAsString);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public int size() {
        LockUtil.lockForRead(lock);
        try {
             return competitorCache.size();
       } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public void clear() {
        LockUtil.lockForWrite(lock);
        try {
            competitorCache.clear();
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }
    
    @Override
    public Iterable<Competitor> getCompetitors() {
        LockUtil.lockForRead(lock);
        try {
            return new ArrayList<Competitor>(competitorCache.values());
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public void removeCompetitor(Competitor competitor) {
        LockUtil.lockForWrite(lock);
        try {
            competitorCache.remove(competitor.getId());
            competitorsByIdAsString.remove(competitor.getId().toString());  
            weakCompetitorDTOCache.remove(competitor);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public Competitor updateCompetitor(String idAsString, String newName, String newSailId, Nationality newNationality) {
        DynamicCompetitor competitor = (DynamicCompetitor) getExistingCompetitorByIdAsString(idAsString);
        if (competitor != null) {
            LockUtil.lockForWrite(lock);
            try {
                competitor.setName(newName);
                competitor.getBoat().setSailId(newSailId);
                competitor.getTeam().setNationality(newNationality);
                weakCompetitorDTOCache.remove(competitor);
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        }
        return competitor;
    }

    @Override
    public CompetitorDTO convertToCompetitorDTO(Competitor c) {
        LockUtil.lockForRead(lock);
        boolean needToUnlockReadLock = true;
        try {
            CompetitorDTO competitorDTO = weakCompetitorDTOCache.get(c);
            if (competitorDTO == null) {
                LockUtil.unlockAfterRead(lock);
                needToUnlockReadLock = false;
                LockUtil.lockForWrite(lock);
                competitorDTO = weakCompetitorDTOCache.get(c);
                if (competitorDTO == null) {
                    final Nationality nationality = c.getTeam().getNationality();
                    CountryCode countryCode = nationality == null ? null : nationality.getCountryCode();
                    competitorDTO = new CompetitorDTOImpl(c.getName(), countryCode == null ? ""
                            : countryCode.getTwoLetterISOCode(), countryCode == null ? ""
                            : countryCode.getThreeLetterIOCCode(), countryCode == null ? "" : countryCode.getName(), c
                            .getBoat().getSailID(), c.getId().toString(), new BoatClassDTO(c.getBoat().getBoatClass()
                            .getName(), c.getBoat().getBoatClass().getHullLength().getMeters()));
                    weakCompetitorDTOCache.put(c, competitorDTO);
                }
            }
            return competitorDTO;
        } finally {
            if (needToUnlockReadLock) {
                LockUtil.unlockAfterRead(lock);
            } else {
                LockUtil.unlockAfterWrite(lock);
            }
        }
    }
    
}
