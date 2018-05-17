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
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTOImpl;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sse.common.Color;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

public class TransientCompetitorAndBoatStoreImpl implements CompetitorAndBoatStore, Serializable {
    private static final Logger logger = Logger.getLogger(TransientCompetitorAndBoatStoreImpl.class.getName());
    private static final long serialVersionUID = -4198298775476586931L;

    private final Map<Serializable, DynamicCompetitor> competitorCache;
    private final Map<String, DynamicCompetitor> competitorsByIdAsString;
    private transient Set<CompetitorUpdateListener> competitorUpdateListeners;
    
    private final Map<Serializable, DynamicBoat> boatCache;
    private final Map<String, DynamicBoat> boatsByIdAsString;
    private transient Set<BoatUpdateListener> boatUpdateListeners;
    
    /**
     * The competitors contained in this map will have their changeable properties
     * {@link #updateCompetitor() updated} upon the next call to {@link #getOrCreateCompetitor()} for their ID.
     */
    private final Set<Competitor> competitorsToUpdateDuringGetOrCreate;
    
    private transient WeakHashMap<Competitor, CompetitorDTO> weakCompetitorDTOCache;

    private final Set<DynamicBoat> boatsToUpdateDuringGetOrCreate;
    
    private transient WeakHashMap<Boat, BoatDTO> weakBoatDTOCache;
    
    private final NamedReentrantReadWriteLock lock;

    public TransientCompetitorAndBoatStoreImpl() {
        lock = new NamedReentrantReadWriteLock("CompetitorStore", /* fair */ false);
        competitorCache = new HashMap<>();
        competitorsByIdAsString = new HashMap<>();
        competitorsToUpdateDuringGetOrCreate = new HashSet<>();
        weakCompetitorDTOCache = new WeakHashMap<>();
        competitorUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorAndBoatStore.CompetitorUpdateListener>());
        boatCache = new HashMap<>();
        boatsByIdAsString = new HashMap<>();
        boatsToUpdateDuringGetOrCreate = new HashSet<>();
        weakBoatDTOCache = new WeakHashMap<>();
        boatUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorAndBoatStore.BoatUpdateListener>());
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        weakCompetitorDTOCache = new WeakHashMap<Competitor, CompetitorDTO>();
        competitorUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorAndBoatStore.CompetitorUpdateListener>());
        weakBoatDTOCache = new WeakHashMap<>();
        boatUpdateListeners = Collections.synchronizedSet(new HashSet<CompetitorAndBoatStore.BoatUpdateListener>());
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

    private DynamicCompetitor createCompetitor(Serializable id, String name, String shortName, Color displayColor, String email, URI flagImage,
            DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        DynamicCompetitor result = new CompetitorImpl(id, name, shortName, displayColor, email, flagImage, team,
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
     * <p>
     * 
     * If a competitor with an ID equal to that of {@code competitor} already exists in this store, an
     * {@link IllegalArgumentException} will be thrown. This is necessary because there is a rule in place saying each
     * competitor entity can be represented in the scope of the VM by at most one Java {@link Competitor} object, and
     * replacing an object in this store would leave us with at least two.
     */
    protected void addNewCompetitor(DynamicCompetitor competitor) {
        LockUtil.lockForWrite(lock);
        try {
            final Competitor existingCompetitorWithEqualId = competitorCache.put(competitor.getId(), competitor);
            if (existingCompetitorWithEqualId != null && existingCompetitorWithEqualId != competitor) {
                final String msg = "Replaced competitor "+existingCompetitorWithEqualId+" with ID "+
                        existingCompetitorWithEqualId.getId()+" of type "+existingCompetitorWithEqualId.getClass().getName()+
                        " by another object "+competitor+" of type "+
                        competitor.getClass().getName()+
                        " that has an equal ID. This is a pretty bad thing because we expect each competitor to be represented by exactly one Java object.";
                logger.severe(msg);
                throw new IllegalArgumentException(msg);
            }
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
    public DynamicCompetitor getOrCreateCompetitor(Serializable competitorId, String name, String shortName, Color displayColor, String email,
            URI flagImage, DynamicTeam team, Double timeOnTimeFactor,
            Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        DynamicCompetitor result = getExistingCompetitorById(competitorId); // avoid synchronization for successful read access
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
    public DynamicCompetitor getExistingCompetitorById(Serializable competitorId) {
        LockUtil.lockForRead(lock);
        try {
            DynamicCompetitor competitor = competitorCache.get(competitorId);
            return competitor;
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public DynamicCompetitor getExistingCompetitorByIdAsString(String competitorIdAsString) {
        LockUtil.lockForRead(lock);
        try {
            return competitorsByIdAsString.get(competitorIdAsString);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public DynamicCompetitorWithBoat getExistingCompetitorWithBoatById(Serializable competitorId) {
        final DynamicCompetitorWithBoat result;
        LockUtil.lockForRead(lock);
        try {
            DynamicCompetitor competitor = competitorCache.get(competitorId);
            if (competitor != null && competitor.hasBoat()) {
                result = (DynamicCompetitorWithBoat) competitor;
            } else {
                result = null;
            }
            return result;
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public CompetitorWithBoat getExistingCompetitorWithBoatByIdAsString(String competitorIdAsString) {
        LockUtil.lockForRead(lock);
        try {
            Competitor competitor = competitorsByIdAsString.get(competitorIdAsString);
            if (competitor != null && competitor.hasBoat()) {
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
        DynamicCompetitor competitor = getExistingCompetitorByIdAsString(idAsString);
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
    public void addNewCompetitors(Iterable<DynamicCompetitor> competitors) {
        for (DynamicCompetitor competitor : competitors) {
            addNewCompetitor(competitor);
        }
    }

    @Override
    public void addNewCompetitorsWithBoat(Iterable<DynamicCompetitorWithBoat> competitors) {
        for (DynamicCompetitorWithBoat competitor : competitors) {
            addNewBoat(competitor.getBoat()); // create the boat before the competitor because the competitor references the boat
            addNewCompetitor(competitor);
        }
    }

    private DynamicCompetitorWithBoat createCompetitorWithBoat(Serializable id, String name, String shortName, Color displayColor, String email, URI flagImage,
            DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag, DynamicBoat boat) {
        DynamicCompetitorWithBoat competitor = new CompetitorWithBoatImpl(id, name, shortName, displayColor, email, flagImage, team,
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag, boat);
        addNewBoat(boat); // create the boat before the competitor because the competitor references the boat
        addNewCompetitor(competitor);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Created competitor "+name+" with ID "+id, new Exception("Here is where it happened"));
        }
        return competitor ;
    }

    @Override
    public DynamicCompetitorWithBoat getOrCreateCompetitorWithBoat(Serializable competitorId, String name, String shortName, Color displayColor, String email,
            URI flagImage, DynamicTeam team, Double timeOnTimeFactor,
            Duration timeOnDistanceAllowancePerNauticalMile, String searchTag, DynamicBoat boat) {
        DynamicCompetitorWithBoat result = null; 
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
            assert result.getBoat() == boat;
            updateCompetitor(result.getId().toString(), name, shortName, displayColor, email, team.getNationality(),
                    team.getImage(), flagImage, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
            competitorNoLongerToUpdateDuringGetOrCreate(result);
        }
        return result;
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

    private DynamicBoat createBoat(Serializable id, String name, BoatClass boatClass, String sailID, Color color) {
        DynamicBoat boat = new BoatImpl(id, name, boatClass, sailID, color);
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
    protected void addNewBoat(DynamicBoat boat) {
        LockUtil.lockForWrite(lock);
        try {
            final Boat existingBoatWithEqualId = boatCache.put(boat.getId(), boat);
            if (existingBoatWithEqualId != null && existingBoatWithEqualId != boat) {
                logger.warning("Replaced existing boat "+existingBoatWithEqualId+" with ID "+existingBoatWithEqualId.getId()+
                        " by another boat with equal ID: "+boat);
            }
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
    public DynamicBoat getOrCreateBoat(Serializable id, String name, BoatClass boatClass, String sailId, Color color) {
        DynamicBoat result = getExistingBoatById(id); // avoid synchronization for successful read access
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
    public DynamicBoat getExistingBoatById(Serializable boatId) {
        LockUtil.lockForRead(lock);
        try {
            return boatCache.get(boatId);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @Override
    public DynamicBoat getExistingBoatByIdAsString(String boatIdAsString) {
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
    public Iterable<DynamicBoat> getStandaloneBoats() {
        LockUtil.lockForRead(lock);
        try {
            Set<DynamicBoat> boats = new HashSet<>(boatCache.values());
            Set<DynamicBoat> boatsEmbeddedInCompetitors = new HashSet<>();
            for (Competitor competitor : competitorCache.values()) {
                if (competitor.hasBoat()) {
                    boatsEmbeddedInCompetitors.add(((DynamicCompetitorWithBoat) competitor).getBoat()); 
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
    public void allowBoatResetToDefaults(DynamicBoat boat) {
        LockUtil.lockForWrite(lock);
        try {
            boatsToUpdateDuringGetOrCreate.add(boat);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }
    
    @Override
    public void addNewBoats(Iterable<DynamicBoat> boats) {
        LockUtil.lockForWrite(lock);
        try {
            for (DynamicBoat boat : boats) {
                boatCache.put(boat.getId(), boat);
                boatsByIdAsString.put(boat.getId().toString(), boat);
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }
    
    @Override
    public CompetitorWithBoatDTO convertToCompetitorWithBoatDTO(CompetitorWithBoat competitor) {
        CompetitorDTO c = convertToCompetitorDTO(competitor);
        BoatDTO boatDTO = convertToBoatDTO(competitor.getBoat()); 
        CompetitorWithBoatDTO competitorDTO = new CompetitorWithBoatDTOImpl(c, boatDTO);
        return competitorDTO;
    }

    @Override
    public CompetitorDTO convertToCompetitorWithOptionalBoatDTO(Competitor competitor) {
        if (competitor.hasBoat()) {
            return convertToCompetitorWithBoatDTO((CompetitorWithBoat) competitor);
        } else {
            return convertToCompetitorDTO(competitor);
        }
    }

    @Override
    public Map<CompetitorDTO, BoatDTO> convertToCompetitorAndBoatDTOs(Map<Competitor, ? extends Boat> competitorsAndBoats) {
        Map<CompetitorDTO, BoatDTO> result = new HashMap<>();
        for (Entry<Competitor, ? extends Boat> entry : competitorsAndBoats.entrySet()) {
            CompetitorDTO competitorDTO = convertToCompetitorWithOptionalBoatDTO(entry.getKey());
            BoatDTO boatDTO = convertToBoatDTO(entry.getValue());
            result.put(competitorDTO, boatDTO);
        }
        return result;
    }

}
