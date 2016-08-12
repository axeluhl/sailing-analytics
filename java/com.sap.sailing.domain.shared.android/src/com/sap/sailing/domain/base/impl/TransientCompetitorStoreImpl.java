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

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
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
    private transient Set<CompetitorUpdateListener> competitorUpdateListeners;
    private final Map<Serializable, Boat> boatCache;
    private final Map<String, Boat> boatsByIdAsString;
    private transient Set<BoatUpdateListener> boatUpdateListeners;
    
    /**
     * The competitors contained in this map will have their changeable properties
     * {@link #updateCompetitor(String, String, String, Nationality) updated} upon the next call to
     * {@link #getOrCreateCompetitor(Serializable, String, DynamicTeam, DynamicBoat)} for their ID.
     */
    private final Set<Competitor> competitorsToUpdateDuringGetOrCreate;
    
    private transient WeakHashMap<Competitor, CompetitorDTO> weakCompetitorDTOCache;

    private final Set<Boat> boatsToUpdateDuringGetOrCreate;
    
    private transient WeakHashMap<Boat, BoatDTO> weakBoatDTOCache;

    private final NamedReentrantReadWriteLock lock;

    public TransientCompetitorStoreImpl() {
        lock = new NamedReentrantReadWriteLock("CompetitorStore", /* fair */ false);
        competitorCache = new HashMap<Serializable, Competitor>();
        competitorsByIdAsString = new HashMap<String, Competitor>();
        competitorsToUpdateDuringGetOrCreate = new HashSet<Competitor>();
        weakCompetitorDTOCache = new WeakHashMap<Competitor, CompetitorDTO>();
        competitorUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorStore.CompetitorUpdateListener>());
        boatCache = new HashMap<Serializable, Boat>();
        boatsByIdAsString = new HashMap<String, Boat>();
        boatsToUpdateDuringGetOrCreate = new HashSet<Boat>();
        weakBoatDTOCache = new WeakHashMap<Boat, BoatDTO>();
        boatUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorStore.BoatUpdateListener>());
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        weakCompetitorDTOCache = new WeakHashMap<Competitor, CompetitorDTO>();
        competitorUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorStore.CompetitorUpdateListener>());
        weakBoatDTOCache = new WeakHashMap<Boat, BoatDTO>();
        boatUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorStore.BoatUpdateListener>());
    }
    
    @Override
    public void addCompetitorUpdateListener(CompetitorUpdateListener listener) {
        competitorUpdateListeners.add(listener);
    }

    @Override
    public void removeCompetitorUpdateListener(CompetitorUpdateListener listener) {
        competitorUpdateListeners.remove(listener);
    }

    private Competitor createCompetitor(Serializable id, String name, String shortName, Color displayColor, String email, URI flagImage,
            DynamicTeam team, DynamicBoat boat, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        Competitor result = new CompetitorImpl(id, name, shortName, displayColor, email, flagImage, team, boat,
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
        addNewCompetitor(id, result);
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
    public Competitor getOrCreateCompetitor(Serializable competitorId, String name, String shortName, Color displayColor, String email,
            URI flagImage, DynamicTeam team, DynamicBoat boat, Double timeOnTimeFactor,
            Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        Competitor result = getExistingCompetitorById(competitorId); // avoid synchronization for successful read access
        if (result == null) {
            LockUtil.lockForWrite(lock);
            try {
                result = getExistingCompetitorById(competitorId); // try again, now while holding the write lock
                if (result == null) {
                    result = createCompetitor(competitorId, name, shortName, displayColor, email, flagImage, team, boat,
                            timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
                }
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        } else if (isCompetitorToUpdateDuringGetOrCreate(result)) {
            updateCompetitor(result.getId().toString(), name, shortName, displayColor, email, boat.getSailID(), team.getNationality(),
                    team.getImage(), flagImage, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
            competitorNoLongerToUpdateDuringGetOrCreate(result);
        }
        return result;
    }
    
    private void competitorNoLongerToUpdateDuringGetOrCreate(Competitor competitor) {
        competitorsToUpdateDuringGetOrCreate.remove(competitor);
    }

    @Override
    public boolean isCompetitorToUpdateDuringGetOrCreate(Competitor competitor) {
        return competitorsToUpdateDuringGetOrCreate.contains(competitor);
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
    public int getCompetitorsCount() {
        LockUtil.lockForRead(lock);
        try {
             return competitorCache.size();
       } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public void clearCompetitors() {
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
    public Competitor updateCompetitor(String idAsString, String newName, String newShortName, Color newDisplayColor, String newEmail,
            String newSailId, Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri,
            Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String newSearchTag) {
        DynamicCompetitor competitor = (DynamicCompetitor) getExistingCompetitorByIdAsString(idAsString);
        if (competitor != null) {
            LockUtil.lockForWrite(lock);
            try {
                competitor.setName(newName);
                competitor.setShortName(newShortName);
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
        synchronized (competitorUpdateListeners) {
            for (CompetitorUpdateListener listener : competitorUpdateListeners) {
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
                    competitorDTO = new CompetitorDTOImpl(c.getName(), c.getShortName(), c.getColor(), c.getEmail(), countryCode == null ? ""
                            : countryCode.getTwoLetterISOCode(), countryCode == null ? ""
                            : countryCode.getThreeLetterIOCCode(), countryCode == null ? "" : countryCode.getName(),
                              c.getId().toString(),
                              c.getTeam().getImage() == null ? null : c.getTeam().getImage().toString(),
                              c.getFlagImage() == null ? null : c.getFlagImage().toString(),
                            new BoatDTO(c.getBoat().getName(), c.getBoat().getSailID(), c.getBoat().getColor()),  
                            new BoatClassDTO(c.getBoat().getBoatClass()
                            .getName(), c.getBoat().getBoatClass().getDisplayName(), c.getBoat().getBoatClass().getHullLength().getMeters()),
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

    /** Boat stuff starts here */
    
    @Override
    public void addBoatUpdateListener(BoatUpdateListener listener) {
        boatUpdateListeners.add(listener);
    }

    @Override
    public void removeBoatUpdateListener(BoatUpdateListener listener) {
        boatUpdateListeners.remove(listener);
    }

    private Boat createBoat(Serializable id, String name, BoatClass boatClass, String sailID, Color color) {
        Boat result = new BoatImpl(id, name, boatClass, sailID, color);
        addNewBoat(result.getId(), result);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Created boat "+name+" with ID "+id, new Exception("Here is where it happened"));
        }
        return result;
    }

    /**
     * Adds the <code>boat</code> to this transient boat collection so that it is available in
     * {@link #getExistingBoatById(Serializable)}. Subclasses may override in case they need to take additional
     * measures such as durably storing the boat. Overriding implementations must call this implementation.
     */
    protected void addNewBoat(Serializable id, Boat boat) {
        LockUtil.lockForWrite(lock);
        try {
            boatCache.put(id, boat);
            boatsByIdAsString.put(id.toString(), boat);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public Boat getOrCreateBoat(Serializable id, String name, BoatClass boatClass, String sailId, Color color) {
        // create compound boat ID here?
        Boat result = getExistingBoatById(id); // avoid synchronization for successful read access
        if (result == null) {
            LockUtil.lockForWrite(lock);
            try {
                result = getExistingBoatById(id); // try again, now while holding the write lock
                if (result == null) {
                    result = createBoat(id, name, boatClass, sailId, color);
                }
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        } else if (isBoatToUpdateDuringGetOrCreate(result)) {
            updateBoat(result.getId().toString(), name, color, sailId);
            boatNoLongerToUpdateDuringGetOrCreate(result);
        }
        return result;
    }

    @Override
    public Boat getOrCreateBoat(LeaderboardGroupBase leaderboardGroup, String name, BoatClass boatClass, String sailId, Color color) {
        return getOrCreateBoat(leaderboardGroup.getId(), name, boatClass, sailId, color);
    }
    
    @Override
    public Boat getOrCreateBoat(Competitor competitor, String name, BoatClass boatClass, String sailId, Color color) {
        return getOrCreateBoat(competitor.getId(), name, boatClass, sailId, color);
    }
    
    private void boatNoLongerToUpdateDuringGetOrCreate(Boat boat) {
        boatsToUpdateDuringGetOrCreate.remove(boat);
    }

    @Override
    public boolean isBoatToUpdateDuringGetOrCreate(Boat boat) {
        return boatsToUpdateDuringGetOrCreate.contains(boat);
    }

    @Override
    public Boat getExistingBoatById(Serializable boatId) {
        LockUtil.lockForRead(lock);
        try {
            return boatCache.get(boatId);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public Boat getExistingBoatByIdAsString(String boatIdAsString) {
        LockUtil.lockForRead(lock);
        try {
            return boatsByIdAsString.get(boatIdAsString);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public int getBoatsCount() {
        LockUtil.lockForRead(lock);
        try {
             return boatCache.size();
       } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public void clearBoats() {
        LockUtil.lockForWrite(lock);
        try {
            boatCache.clear();
            boatsByIdAsString.clear();
            boatsToUpdateDuringGetOrCreate.clear();
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Clearing boat store "+this, new Exception("here is where it happened"));
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }
    
    @Override
    public Iterable<Boat> getBoats() {
        LockUtil.lockForRead(lock);
        try {
            return new ArrayList<Boat>(boatCache.values());
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public void removeBoat(Boat boat) {
        LockUtil.lockForWrite(lock);
        try {
            logger.fine("Removing boat "+boat+" from boat store "+this);
            boatCache.remove(boat.getId());
            boatsByIdAsString.remove(boat.getId().toString());  
            weakBoatDTOCache.remove(boat);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public Boat updateBoat(String idAsString, String newName, Color newColor, String newSailId) {
        DynamicBoat boat = (DynamicBoat) getExistingBoatByIdAsString(idAsString);
        if (boat != null) {
            LockUtil.lockForWrite(lock);
            try {
                boat.setName(newName);
                boat.setSailId(newSailId);
                boat.setColor(newColor);
                weakBoatDTOCache.remove(boat);
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        }
        synchronized (boatUpdateListeners) {
            for (BoatUpdateListener listener : boatUpdateListeners) {
                listener.boatUpdated(boat);
            }
        }
        return boat;
    }

    @Override
    public BoatDTO convertToBoatDTO(Boat b) {
        LockUtil.lockForRead(lock);
        boolean needToUnlockReadLock = true;
        try {
            BoatDTO boatDTO = weakBoatDTOCache.get(b);
            if (boatDTO == null) {
                LockUtil.unlockAfterRead(lock);
                needToUnlockReadLock = false;
                LockUtil.lockForWrite(lock);
                boatDTO = weakBoatDTOCache.get(b);
                if (boatDTO == null) {
                    boatDTO = new BoatDTO(b.getName(), b.getSailID(), b.getColor());
                    weakBoatDTOCache.put(b, boatDTO);
                }
            }
            return boatDTO;
        } finally {
            if (needToUnlockReadLock) {
                LockUtil.unlockAfterRead(lock);
            } else {
                LockUtil.unlockAfterWrite(lock);
            }
        }
    }

    @Override
    public void allowBoatResetToDefaults(Boat boat) {
        LockUtil.lockForWrite(lock);
        try {
            boatsToUpdateDuringGetOrCreate.add(boat);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }
}
