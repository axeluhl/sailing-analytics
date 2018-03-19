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
    public void clear() {
        clearCompetitors();
        clearBoats();
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
        addNewCompetitor(result);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Created competitor "+name+" with ID "+id, new Exception("Here is where it happened"));
        }
        return result;
    }

    @Override
    public synchronized Competitor migrateToCompetitorWithoutBoat(CompetitorWithBoat competitorWithBoat) {
        // It should not be possible to call this for a competitor which has already been migrated.
        assert competitorWithBoat.hasBoat();
        // We can't create a new competitor without boat here as the existing competitorWithBoat might be already referenced.
        // Therefore we only clear the 'boat' property. 
        removeBoat(competitorWithBoat.getBoat());
        ((DynamicCompetitorWithBoat) competitorWithBoat).clearBoat();        
        return competitorWithBoat;
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
        synchronized (competitorUpdateListeners) {
            for (final CompetitorUpdateListener listener : competitorUpdateListeners) {
                listener.competitorCreated(competitor);
            }
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
            return competitor;
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

    protected boolean isValidCompetitorWithBoat(Competitor competitor) {
        assert competitor != null;
        return competitor.hasBoat();
    }

    @Override
    public CompetitorWithBoat getExistingCompetitorWithBoatById(Serializable competitorId) {
        LockUtil.lockForRead(lock);
        try {
            Competitor competitor = competitorCache.get(competitorId);
            if (competitor != null && isValidCompetitorWithBoat(competitor)) {
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
            if (competitor != null && isValidCompetitorWithBoat(competitor)) {
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
    public Iterable<Competitor> getAllCompetitors() {
        LockUtil.lockForRead(lock);
        try {
            return new ArrayList<Competitor>(competitorCache.values());
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public Iterable<CompetitorWithBoat> getCompetitorsWithBoat() {
        LockUtil.lockForRead(lock);
        try {
            List<CompetitorWithBoat> competitors = new ArrayList<>();
            for (Competitor c: competitorCache.values()) {
                if (c.hasBoat()) {
                    competitors.add((CompetitorWithBoat) c);
                }
            }
            return competitors;
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public Iterable<Competitor> getCompetitorsWithoutBoat() {
        LockUtil.lockForRead(lock);
        try {
            List<Competitor> competitors = new ArrayList<>();
            for (Competitor c: competitorCache.values()) {
                if (!c.hasBoat()) {
                    competitors.add(c);
                }
            }
            return competitors;
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    protected void removeCompetitor(Competitor competitor) {
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
    public CompetitorWithoutBoatDTO convertToCompetitorDTO(Competitor c) {
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

    @Override
    public void addNewCompetitorsWithBoat(Iterable<CompetitorWithBoat> competitors) {
        for (CompetitorWithBoat competitor: competitors) {
            addNewCompetitor(competitor);
            addNewBoat(competitor.getBoat());
        }
    }

    private CompetitorWithBoat createCompetitorWithBoat(Serializable id, String name, String shortName, Color displayColor, String email, URI flagImage,
            DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag, DynamicBoat boat) {
        CompetitorWithBoat competitor = new CompetitorWithBoatImpl(id, name, shortName, displayColor, email, flagImage, team,
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag, boat);
        addNewCompetitor(competitor);
        addNewBoat(boat);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Created competitor "+name+" with ID "+id, new Exception("Here is where it happened"));
        }
        return competitor ;
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
                DynamicBoat boatOfCompetitor = competitor.getBoat();
                if (boatOfCompetitor != null && boat != null) {
                    boatOfCompetitor.setName(boat.getName());
                    boatOfCompetitor.setSailId(boat.getSailID());
                    boatOfCompetitor.setColor(boat.getColor());
                }
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
        Boat boat = new BoatImpl(id, name, boatClass, sailID, color);
        addNewBoat(boat);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Created boat "+name+" with ID "+id, new Exception("Here is where it happened"));
        }
        return boat;
    }

    /**
     * Adds the <code>boat</code> to this transient boat collection so that it is available in
     * {@link #getExistingBoatById(Serializable)}. Subclasses may override in case they need to take additional
     * measures such as durably storing the boat. Overriding implementations must call this implementation.
     */
    protected void addNewBoat(Boat boat) {
        LockUtil.lockForWrite(lock);
        try {
            boatCache.put(boat.getId(), boat);
            boatsByIdAsString.put(boat.getId().toString(), boat);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
        synchronized (boatUpdateListeners) {
            for (final BoatUpdateListener listener : boatUpdateListeners) {
                listener.boatCreated(boat);
            }
        }
    }

    @Override
    public Boat getOrCreateBoat(Serializable id, String name, BoatClass boatClass, String sailId, Color color) {
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
                if (isValidCompetitorWithBoat(competitor)) {
                    boatsEmbeddedInCompetitors.add(((CompetitorWithBoat) competitor).getBoat()); 
                }
            }
            boats.removeAll(boatsEmbeddedInCompetitors);
            return boats;
        } finally {
            LockUtil.unlockAfterRead(lock);
        }

    }

    protected void removeBoat(Boat boat) {
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
    public void addNewBoats(Iterable<Boat> boats) {
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
    
    @Override
    public CompetitorDTO convertToCompetitorWithBoatDTO(Competitor competitor, Boat boat) {
        CompetitorWithoutBoatDTO c = convertToCompetitorDTO(competitor);
        BoatDTO boatDTO = null;
        if (boat != null) {
            boatDTO = convertToBoatDTO(boat); 
        }
        CompetitorDTO competitorDTO = new CompetitorDTOImpl(c, boatDTO);

        return competitorDTO;
    }

    @Override
    public CompetitorDTO convertToCompetitorWithOptionalBoatDTO(Competitor competitor) {
        if (isValidCompetitorWithBoat(competitor)) {
            return convertToCompetitorWithBoatDTO(competitor, ((CompetitorWithBoat) competitor).getBoat());
        } else {
            return convertToCompetitorWithBoatDTO(competitor, null);
        }
    }

    @Override
    public Map<CompetitorDTO, BoatDTO> convertToCompetitorAndBoatDTOs(Map<Competitor, Boat> competitorsAndBoats) {
        Map<CompetitorDTO, BoatDTO> result = new HashMap<>();
        for (Entry<Competitor, Boat> entry: competitorsAndBoats.entrySet()) {
            CompetitorDTO competitorDTO = convertToCompetitorWithOptionalBoatDTO(entry.getKey());
            BoatDTO boatDTO = convertToBoatDTO(entry.getValue());
            result.put(competitorDTO, boatDTO);
        }
        return result;
    }

}
