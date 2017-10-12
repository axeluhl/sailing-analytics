package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.dto.CompetitorWithoutBoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithoutBoatDTOImpl;
import com.sap.sse.common.Color;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
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
     * {@link #updateCompetitor() updated} upon the next call to {@link #getOrCreateCompetitor()} for their ID.
     */
    private final Set<Competitor> competitorsToUpdateDuringGetOrCreate;
    
    private transient WeakHashMap<Competitor, CompetitorWithoutBoatDTO> weakCompetitorDTOCache;


    private final Set<Boat> boatsToUpdateDuringGetOrCreate;
    
    private transient WeakHashMap<Boat, BoatDTO> weakBoatDTOCache;

    private final NamedReentrantReadWriteLock lock;

    public TransientCompetitorStoreImpl() {
        lock = new NamedReentrantReadWriteLock("CompetitorStore", /* fair */ false);
        competitorCache = new HashMap<Serializable, Competitor>();
        competitorsByIdAsString = new HashMap<String, Competitor>();
        competitorsToUpdateDuringGetOrCreate = new HashSet<Competitor>();
        weakCompetitorDTOCache = new WeakHashMap<Competitor, CompetitorWithoutBoatDTO>();
        competitorUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorStore.CompetitorUpdateListener>());

        boatCache = new HashMap<Serializable, Boat>();
        boatsByIdAsString = new HashMap<String, Boat>();
        boatsToUpdateDuringGetOrCreate = new HashSet<Boat>();
        weakBoatDTOCache = new WeakHashMap<Boat, BoatDTO>();
        boatUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorStore.BoatUpdateListener>());
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        weakCompetitorDTOCache = new WeakHashMap<Competitor, CompetitorWithoutBoatDTO>();
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
            DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        Competitor result = new CompetitorImpl(id, name, shortName, displayColor, email, flagImage, team,
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
        addNewCompetitor(id, result);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Created competitor "+name+" with ID "+id, new Exception("Here is where it happened"));
        }
        return result;
    }

    @Override
    public Pair<Competitor, Boat> migrateCompetitorToHaveASeparateBoat(Serializable boatId, CompetitorWithBoat competitorWithBoat) {
        Boat existingBoat = competitorWithBoat.getBoat();
        Competitor newCompetitor = getOrCreateCompetitor(competitorWithBoat.getId(), competitorWithBoat.getName(), competitorWithBoat.getShortName(),
                competitorWithBoat.getColor(), competitorWithBoat.getEmail(), competitorWithBoat.getFlagImage(), (DynamicTeam) competitorWithBoat.getTeam(),
                competitorWithBoat.getTimeOnTimeFactor(), competitorWithBoat.getTimeOnDistanceAllowancePerNauticalMile(), competitorWithBoat.getSearchTag());
        Boat newBoat = getOrCreateBoat(boatId, existingBoat.getName(), existingBoat.getBoatClass(), existingBoat.getSailID(), existingBoat.getColor());
        addNewCompetitor(newCompetitor.getId(), newCompetitor);
        addNewBoat(newBoat.getId(), newBoat);
        return new Pair<>(newCompetitor, newBoat);
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
            URI flagImage, DynamicTeam team, Double timeOnTimeFactor,
            Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        Competitor result = getExistingCompetitorById(competitorId); // avoid synchronization for successful read access
        if (result == null) {
            LockUtil.lockForWrite(lock);
            try {
                result = getExistingCompetitorById(competitorId); // try again, now while holding the write lock
                if (result == null) {
                    result = createCompetitor(competitorId, name, shortName, displayColor, email, flagImage, team,
                            timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
                }
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        } else if (isCompetitorToUpdateDuringGetOrCreate(result)) {
            updateCompetitor(result.getId().toString(), name, shortName, displayColor, email, team.getNationality(),
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
            Competitor competitor = competitorCache.get(competitorId);
            if (!(competitor instanceof CompetitorWithBoat)) {
                return competitor;
            } else {
                return null;
            }        
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public Competitor getExistingCompetitorByIdAsString(String competitorIdAsString) {
        LockUtil.lockForRead(lock);
        try {
            Competitor competitor = competitorsByIdAsString.get(competitorIdAsString);
            if (!(competitor instanceof CompetitorWithBoat)) {
                return competitor;
            } else {
                return null;
            }        
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    public CompetitorWithBoat getExistingCompetitorWithBoatById(Serializable competitorId) {
        LockUtil.lockForRead(lock);
        try {
            Competitor competitor = competitorCache.get(competitorId);
            if (competitor instanceof CompetitorWithBoat) {
                return (CompetitorWithBoat) competitor;
            } else {
                return null;
            }        
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public CompetitorWithBoat getExistingCompetitorWithBoatByIdAsString(String competitorIdAsString) {
        LockUtil.lockForRead(lock);
        try {
            Competitor competitor = competitorsByIdAsString.get(competitorIdAsString);
            if (competitor instanceof CompetitorWithBoat) {
                return (CompetitorWithBoat) competitor;
            } else {
                return null;
            }        
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
            Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri,
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
    public CompetitorWithoutBoatDTO convertToCompetitorWithoutBoatDTO(Competitor c) {
        LockUtil.lockForRead(lock);
        boolean needToUnlockReadLock = true;
        try {
            CompetitorWithoutBoatDTO competitorDTO = weakCompetitorDTOCache.get(c);
            if (competitorDTO == null) {
                LockUtil.unlockAfterRead(lock);
                needToUnlockReadLock = false;
                LockUtil.lockForWrite(lock);
                competitorDTO = weakCompetitorDTOCache.get(c);
                if (competitorDTO == null) {
                    final Nationality nationality = c.getTeam().getNationality();
                    CountryCode countryCode = nationality == null ? null : nationality.getCountryCode();
                    competitorDTO = new CompetitorWithoutBoatDTOImpl(c.getName(), c.getShortName(), c.getColor(), c.getEmail(), countryCode == null ? ""
                            : countryCode.getTwoLetterISOCode(), countryCode == null ? ""
                            : countryCode.getThreeLetterIOCCode(), countryCode == null ? "" : countryCode.getName(),
                              c.getId().toString(),
                              c.getTeam().getImage() == null ? null : c.getTeam().getImage().toString(),
                              c.getFlagImage() == null ? null : c.getFlagImage().toString(),
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
    public CompetitorDTO convertToCompetitorDTO(Competitor competitor, Boat boat) {
        CompetitorWithoutBoatDTO c = convertToCompetitorWithoutBoatDTO(competitor);
        BoatDTO boatDTO = null;
        if (boat != null) {
            boatDTO = convertToBoatDTO(boat); 
        }
        CompetitorDTO competitorDTO = new CompetitorDTOImpl(c, boatDTO);

        return competitorDTO;
    }

    @Override
    public Map<CompetitorDTO, BoatDTO> convertToCompetitorAndBoatDTOs(Map<Competitor, Boat> competitorsAndBoats) {
        Map<CompetitorDTO, BoatDTO> result = new HashMap<>();
        for (Entry<Competitor, Boat> entry: competitorsAndBoats.entrySet()) {
            CompetitorWithoutBoatDTO c = convertToCompetitorWithoutBoatDTO(entry.getKey());
            BoatDTO boatDTO = convertToBoatDTO(entry.getValue());
            CompetitorDTO competitorDTO = new CompetitorDTOImpl(c, null);
            result.put(competitorDTO, boatDTO);
        }
        return result;
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
    public void addCompetitors(Iterable<Competitor> competitors) {
        LockUtil.lockForWrite(lock);
        try {
            for (Competitor competitor: competitors) {
                competitorCache.put(competitor.getId(), competitor);
                competitorsByIdAsString.put(competitor.getId().toString(), competitor);
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    private CompetitorWithBoat createCompetitorWithBoat(Serializable id, String name, String shortName, Color displayColor, String email, URI flagImage,
            DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag, DynamicBoat boat) {
        CompetitorWithBoat result = new CompetitorWithBoatImpl(id, name, shortName, displayColor, email, flagImage, team,
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag, boat);
        addNewCompetitor(id, result);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Created competitor "+name+" with ID "+id, new Exception("Here is where it happened"));
        }
        return result;
    }

    @Override
    public CompetitorWithBoat getOrCreateCompetitorWithBoat(Serializable competitorId, String name, String shortName, Color displayColor, String email,
            URI flagImage, DynamicTeam team, Double timeOnTimeFactor,
            Duration timeOnDistanceAllowancePerNauticalMile, String searchTag, DynamicBoat boat) {
        CompetitorWithBoat result = null; 
        result = getExistingCompetitorWithBoatById(competitorId); // avoid synchronization for successful read access
        if (result == null) {
            LockUtil.lockForWrite(lock);
            try {
                result = getExistingCompetitorWithBoatById(competitorId); // try again, now while holding the write lock
                if (result == null) {
                    result = createCompetitorWithBoat(competitorId, name, shortName, displayColor, email, flagImage, team,
                            timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag, boat);
                }
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        } else if (isCompetitorToUpdateDuringGetOrCreate(result)) {
            updateCompetitorWithBoat(result.getId().toString(), name, shortName, displayColor, email, team.getNationality(),
                    team.getImage(), flagImage, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag, boat);
            competitorNoLongerToUpdateDuringGetOrCreate(result);
        }
        return result;
    }
        
    @Override
    public CompetitorWithBoat updateCompetitorWithBoat(String idAsString, String newName, String newShortName, Color newDisplayColor, String newEmail,
            Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri,
            Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String newSearchTag, DynamicBoat boat) {
        DynamicCompetitorWithBoat competitor = (DynamicCompetitorWithBoat) getExistingCompetitorByIdAsString(idAsString);
        if (competitor != null) {
            LockUtil.lockForWrite(lock);
            try {
                competitor.setName(newName);
                competitor.setShortName(newShortName);
                competitor.setColor(newDisplayColor);
                competitor.setEmail(newEmail);
                competitor.setFlagImage(newFlagImageUri);
                competitor.getTeam().setNationality(newNationality);
                competitor.getTeam().setImage(newTeamImageUri);
                competitor.setTimeOnTimeFactor(timeOnTimeFactor);
                competitor.setTimeOnDistanceAllowancePerNauticalMile(timeOnDistanceAllowancePerNauticalMile);
                competitor.setSearchTag(newSearchTag);
                competitor.setBoat(boat);
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
        return (CompetitorWithBoat) competitor;
    }

    @Override
    public CompetitorDTO convertToCompetitorDTO(CompetitorWithBoat competitorWithBoat) {
        return convertToCompetitorDTO(competitorWithBoat, competitorWithBoat.getBoat());
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
    public Iterable<Boat> getStandaloneBoats() {
        LockUtil.lockForRead(lock);
        try {
            List<Boat> boats = new ArrayList<>(boatCache.values());
            Set<Boat> boatsEmbeddedInCompetitors = new HashSet<>();
            for (Competitor competitor: competitorCache.values()) {
                if (competitor instanceof CompetitorWithBoat) {
                    boatsEmbeddedInCompetitors.add(((CompetitorWithBoat) competitor).getBoat()); 
                }
            }
            boats.removeAll(boatsEmbeddedInCompetitors);
            return boats;
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
                    BoatClassDTO boatClassDTO = new BoatClassDTO(b.getBoatClass().getName(), 
                            b.getBoatClass().getDisplayName(), b.getBoatClass().getHullLength(), b.getBoatClass().getHullBeam());
                    boatDTO = new BoatDTO(b.getId().toString(), b.getName(), boatClassDTO, b.getSailID(), b.getColor());
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
    
    @Override
    public void addBoats(Iterable<Boat> boats) {
        LockUtil.lockForWrite(lock);
        try {
            for (Boat boat: boats) {
                boatCache.put(boat.getId(), boat);
                boatsByIdAsString.put(boat.getId().toString(), boat);
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }
}
