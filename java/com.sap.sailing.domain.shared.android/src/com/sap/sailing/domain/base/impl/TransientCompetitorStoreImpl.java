package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sse.common.Color;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

public class TransientCompetitorStoreImpl implements CompetitorStore, Serializable {
    private static final Logger logger = Logger.getLogger(TransientCompetitorStoreImpl.class.getName());
    private static final long serialVersionUID = -4198298775476586931L;
    private final Map<Serializable, Competitor> competitorCache;
    private final Map<String, Competitor> competitorsByIdAsString;
    private transient Set<CompetitorUpdateListener> listeners;
    
    /**
     * The competitors contained in this map will have their changeable properties
     * {@link #updateCompetitor(String, String, String, Nationality) updated} upon the next call to
     * {@link #getOrCreateCompetitor(Serializable, String, DynamicTeam, DynamicBoat)} for their ID.
     */
    private final Set<Competitor> competitorsToUpdateDuringGetOrCreate;
    
    private transient WeakHashMap<Competitor, CompetitorDTO> weakCompetitorDTOCache;
    
    private final NamedReentrantReadWriteLock lock;

    public TransientCompetitorStoreImpl() {
        lock = new NamedReentrantReadWriteLock("CompetitorStore", /* fair */ false);
        competitorCache = new HashMap<Serializable, Competitor>();
        competitorsByIdAsString = new HashMap<String, Competitor>();
        competitorsToUpdateDuringGetOrCreate = new HashSet<Competitor>();
        weakCompetitorDTOCache = new WeakHashMap<Competitor, CompetitorDTO>();
        listeners = Collections.synchronizedSet(new HashSet<CompetitorStore.CompetitorUpdateListener>());
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        weakCompetitorDTOCache = new WeakHashMap<Competitor, CompetitorDTO>();
        listeners = Collections.synchronizedSet(new HashSet<CompetitorStore.CompetitorUpdateListener>());
    }
    
    @Override
    public void addCompetitorUpdateListener(CompetitorUpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeCompetitorUpdateListener(CompetitorUpdateListener listener) {
        listeners.remove(listener);
    }

    private Competitor createCompetitor(Serializable id, String name, Color displayColor, String email, URI flagImage,
            DynamicTeam team, DynamicBoat boat, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        Competitor result = new CompetitorImpl(id, name, displayColor, email, flagImage, team, boat,
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
        addNewCompetitor(result);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Created competitor "+name+" with ID "+id, new Exception("Here is where it happened"));
        }
        return result;
    }

    /**
     * Adds the <code>competitor</code> to this transient competitor collection so that it is available in
     * {@link #getExistingCompetitorById(Serializable)}. Subclasses may override in case they need to take additional
     * measures such as durably storing the competitor. Overriding implementations must call this implementation.
     */
    protected void addNewCompetitor(Competitor competitor) {
        LockUtil.lockForWrite(lock);
        try {
            competitorCache.put(competitor.getId(), competitor);
            competitorsByIdAsString.put(competitor.getId().toString(), competitor);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
        synchronized (listeners) {
            for (final CompetitorUpdateListener listener : listeners) {
                listener.competitorCreated(competitor);
            }
        }
    }
    
    @Override
    public Competitor getOrCreateCompetitor(Serializable competitorId, String name, Color displayColor, String email,
            URI flagImage, DynamicTeam team, DynamicBoat boat, Double timeOnTimeFactor,
            Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        Competitor result = getExistingCompetitorById(competitorId); // avoid synchronization for successful read access
        if (result == null) {
            LockUtil.lockForWrite(lock);
            try {
                result = getExistingCompetitorById(competitorId); // try again, now while holding the write lock
                if (result == null) {
                    result = createCompetitor(competitorId, name, displayColor, email, flagImage, team, boat,
                            timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
                }
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        } else if (isCompetitorToUpdateDuringGetOrCreate(result)) {
            updateCompetitor(result.getId().toString(), name, displayColor, email, boat.getSailID(), team.getNationality(),
                    team.getImage(), flagImage, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
            competitorNoLongerToUpdateDuringGetOrCreate(result);
        }
        return result;
    }
    
    private void competitorNoLongerToUpdateDuringGetOrCreate(Competitor result) {
        competitorsToUpdateDuringGetOrCreate.remove(result);
    }

    @Override
    public boolean isCompetitorToUpdateDuringGetOrCreate(Competitor result) {
        return competitorsToUpdateDuringGetOrCreate.contains(result);
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
            competitorsByIdAsString.clear();
            competitorsToUpdateDuringGetOrCreate.clear();
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Clearing competitor store "+this, new Exception("here is where it happened"));
            }
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
            logger.fine("removing competitor "+competitor+" from competitor store "+this);
            competitorCache.remove(competitor.getId());
            competitorsByIdAsString.remove(competitor.getId().toString());  
            weakCompetitorDTOCache.remove(competitor);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public Competitor updateCompetitor(String idAsString, String newName, Color newDisplayColor, String newEmail,
            String newSailId, Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri,
            Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String newSearchTag) {
        DynamicCompetitor competitor = (DynamicCompetitor) getExistingCompetitorByIdAsString(idAsString);
        if (competitor != null) {
            LockUtil.lockForWrite(lock);
            try {
                competitor.setName(newName);
                competitor.setColor(newDisplayColor);
                competitor.setEmail(newEmail);
                competitor.setFlagImage(newFlagImageUri);
                competitor.getBoat().setSailId(newSailId);
                competitor.getTeam().setNationality(newNationality);
                competitor.getTeam().setImage(newTeamImageUri);
                competitor.setTimeOnTimeFactor(timeOnTimeFactor);
                competitor.setTimeOnDistanceAllowancePerNauticalMile(timeOnDistanceAllowancePerNauticalMile);
                competitor.setSearchTag(newSearchTag);
                weakCompetitorDTOCache.remove(competitor);
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        }
        synchronized (listeners) {
            for (CompetitorUpdateListener listener : listeners) {
                listener.competitorUpdated(competitor);
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
                    competitorDTO = new CompetitorDTOImpl(c.getName(), c.getColor(), c.getEmail(), countryCode == null ? ""
                            : countryCode.getTwoLetterISOCode(), countryCode == null ? ""
                            : countryCode.getThreeLetterIOCCode(), countryCode == null ? "" : countryCode.getName(),
                              c.getId().toString(),
                              c.getTeam().getImage() == null ? null : c.getTeam().getImage().toString(),
                              c.getFlagImage() == null ? null : c.getFlagImage().toString(),
                            new BoatDTO(c.getBoat().getName(), c.getBoat().getSailID(), c.getBoat().getColor()),  
                            new BoatClassDTO(c.getBoat().getBoatClass()
                            .getName(), c.getBoat().getBoatClass().getDisplayName(), c.getBoat().getBoatClass().getHullLength(),
                            c.getBoat().getBoatClass().getHullBeam()),
                            c.getTimeOnTimeFactor(), c.getTimeOnDistanceAllowancePerNauticalMile(), c.getSearchTag());
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

    @Override
    public void allowCompetitorResetToDefaults(Competitor competitor) {
        LockUtil.lockForWrite(lock);
        try {
            competitorsToUpdateDuringGetOrCreate.add(competitor);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public void addNewCompetitors(Iterable<Competitor> competitors) {
        for (Competitor competitor : competitors) {
            addNewCompetitor(competitor);
        }
    }
}
