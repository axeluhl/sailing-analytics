package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorChangeListener;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class CompetitorImpl implements DynamicCompetitor {
    private static final long serialVersionUID = 294603681016643157L;
    private final DynamicTeam team;
    private final Serializable id;
    private String name;
    private String shortName;
    private String searchTag;
    private Color color;
    private transient Set<CompetitorChangeListener> listeners;
    private String email;
    private URI flagImage;
    private Double timeOnTimeFactor;
    private Duration timeOnDistanceAllowancePerNauticalMile;

    public CompetitorImpl(Serializable id, String name, String shortName, Color color, String email, URI flagImage, DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.team = team;
        this.color = color;
        this.email = email;
        this.flagImage = flagImage;
        this.timeOnTimeFactor = timeOnTimeFactor;
        this.timeOnDistanceAllowancePerNauticalMile = timeOnDistanceAllowancePerNauticalMile;
        this.searchTag = searchTag;
        this.listeners = new HashSet<CompetitorChangeListener>();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        listeners = new HashSet<CompetitorChangeListener>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public boolean hasBoat() {
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void setName(String newName) {
        final String oldName = this.name;
        if (!Util.equalsWithNull(oldName, newName)) {
            this.name = newName;
            for (CompetitorChangeListener listener : getListeners()) {
                listener.nameChanged(oldName, newName);
            }
        }
    }

    @Override
    public void setShortName(String newShortName) {
        final String oldShortName = this.shortName;
        if (!Util.equalsWithNull(oldShortName, newShortName)) {
            this.shortName = newShortName;
            for (CompetitorChangeListener listener : getListeners()) {
                listener.shortNameChanged(oldShortName, newShortName);
            }
        }
    }

    @Override
    public String getDisplayName() {
        if (shortName == null || shortName.length() == 0) {
            return name;
        }
        return shortName + Competitor.DELIMITER_SAIL_ID;
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public DynamicTeam getTeam() {
        return team;
    }

    public Nationality getNationality() {
        return getTeam() == null ? null : getTeam().getNationality();
    }

    public Competitor resolve(SharedDomainFactory domainFactory) {
        Competitor result = domainFactory
                .getOrCreateCompetitor(getId(), getName(), getShortName(), getColor(), getEmail(), getFlagImage(), getTeam(),
                        getTimeOnTimeFactor(), getTimeOnDistanceAllowancePerNauticalMile(), searchTag);
        return result;
    }

    @Override
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        final Color oldColor = this.color;
        if (!Util.equalsWithNull(oldColor, color)) {
            this.color = color;
            for (CompetitorChangeListener listener : getListeners()) {
                listener.colorChanged(oldColor, color);
            }
        }
    }

    @Override
    public void addCompetitorChangeListener(CompetitorChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
        getTeam().addNationalityChangeListener(listener);
    }

    @Override
    public void removeCompetitorChangeListener(CompetitorChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
        getTeam().removeNationalityChangeListener(listener);
    }

    private Iterable<CompetitorChangeListener> getListeners() {
        synchronized (listeners) {
            return new HashSet<CompetitorChangeListener>(listeners);
        }
    }

    @Override
    public String getSearchTag() {
        return searchTag;
    }

    public void setSearchTag(String newSearchTag) {
        final String oldSearchTag = this.searchTag;
        if (!Util.equalsWithNull(oldSearchTag, newSearchTag)) {
            this.searchTag = newSearchTag;
            for (CompetitorChangeListener listener : getListeners()) {
                listener.searchTagChanged(oldSearchTag, newSearchTag);
            }
        }
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        final String oldEmail = this.email;
        if (!Util.equalsWithNull(oldEmail, newEmail)) {
            this.email = newEmail;
            for (CompetitorChangeListener listener : getListeners()) {
                listener.emailChanged(oldEmail, newEmail);
            }
        }
    }

    @Override
    public boolean hasEmail() {
        return email != null && !email.isEmpty();
    }

    @Override
    public URI getFlagImage() {
        return flagImage;
    }

    @Override
    public void setFlagImage(URI flagImage) {
        final URI oldFlagImage = this.flagImage;
        this.flagImage = flagImage;
        if (!Util.equalsWithNull(oldFlagImage, flagImage)) {
            for (CompetitorChangeListener listener : getListeners()) {
                listener.flagImageChanged(oldFlagImage, flagImage);
            }
        }
    }

    @Override
    public Double getTimeOnTimeFactor() {
        return timeOnTimeFactor;
    }

    @Override
    public Duration getTimeOnDistanceAllowancePerNauticalMile() {
        return timeOnDistanceAllowancePerNauticalMile;
    }

    @Override
    public void setTimeOnTimeFactor(Double timeOnTimeFactor) {
        Double oldTimeOnTimeFactor = this.timeOnTimeFactor;
        this.timeOnTimeFactor = timeOnTimeFactor;
        if (!Util.equalsWithNull(oldTimeOnTimeFactor, timeOnTimeFactor)) {
            for (CompetitorChangeListener listener : getListeners()) {
                listener.timeOnTimeFactorChanged(oldTimeOnTimeFactor, timeOnTimeFactor);
            }
        }
    }

    @Override
    public void setTimeOnDistanceAllowancePerNauticalMile(Duration timeOnDistanceAllowancePerNauticalMile) {
        Duration oldTimeOnDistanceAllowancePerNauticalMile = this.timeOnDistanceAllowancePerNauticalMile;
        this.timeOnDistanceAllowancePerNauticalMile = timeOnDistanceAllowancePerNauticalMile;
        if (!Util.equalsWithNull(oldTimeOnDistanceAllowancePerNauticalMile, timeOnDistanceAllowancePerNauticalMile)) {
            for (CompetitorChangeListener listener : getListeners()) {
                listener.timeOnDistanceAllowancePerNauticalMileChanged(oldTimeOnDistanceAllowancePerNauticalMile, timeOnDistanceAllowancePerNauticalMile);
            }
        }
    }

}
