package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RaceCompetitorIdsAsStringWithMD5Hash;
import com.sap.sse.common.impl.NamedImpl;

public class RaceDefinitionImpl extends NamedImpl implements RaceDefinition {
    private static final Logger logger = Logger.getLogger(RaceDefinitionImpl.class.getName());
    
    private static final long serialVersionUID = -1900955198751393727L;
    private final Course course;
    private final LinkedHashMap<Serializable, Competitor> competitorsById;
    private final Set<Competitor> competitors;
    private final Map<Competitor, Boat> competitorBoats;
    private final BoatClass boatClass;
    private final Serializable id;
    private final RaceCompetitorIdsAsStringWithMD5Hash raceCompetitorsMD5Hash;

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors) {
        this(name, course, boatClass, competitors, Collections.emptyMap());
    }

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors,
            Map<Competitor, Boat> competitorsAndTheirBoats) {
        this(name, course, boatClass, competitors, competitorsAndTheirBoats, /* use name as default ID */ name);
    }

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors, Serializable id) {
        this(name, course, boatClass, competitors, /* per-race boats for competitors */ Collections.emptyMap(), id);
    }
    
    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors, 
            Map<Competitor, Boat> competitorsAndTheirBoats, Serializable id) {
        super(name);
        assert name != null;
        this.course = course;
        this.competitors = new HashSet<>();
        this.competitorsById = new LinkedHashMap<>();
        this.competitorBoats = new HashMap<>();
        final Set<String> idsOfCompetitorsAsString = new HashSet<>();
        for (Competitor competitor : competitors) {
            Competitor competitorWithEqualID = competitorsById.put(competitor.getId(), competitor);
            this.competitors.add(competitor);
            idsOfCompetitorsAsString.add(competitor.getId().toString());
            if (competitorWithEqualID != null && competitorWithEqualID != competitor) {
                throw new IllegalArgumentException("Two distinct competitors with equal ID "+competitor.getId()+" are not allowed within the single race "+name);
            }
        }
        for (Entry<Competitor, Boat> competitorAndBoat : competitorsAndTheirBoats.entrySet()) {
            Competitor competitor = competitorsById.get(competitorAndBoat.getKey().getId()); // only assign boat if competitor is part of race
            if (competitor != null && competitorAndBoat.getValue() != null) {
                competitorBoats.put(competitor, competitorAndBoat.getValue());
            } else {
                logger.warning("Trying to set boat "+competitorAndBoat.getValue()+" for competitor "+competitorAndBoat.getKey()+
                        " which is not part of race "+getName()+"'s set of competitors");
            }
        }
        try {
            raceCompetitorsMD5Hash = new RaceCompetitorIdsAsStringWithMD5Hash(idsOfCompetitorsAsString);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Internal error: issue with UTF8 or MD5 for encoding competitor IDs as MD5 hash", e);
        }
        this.boatClass = boatClass;
        this.id = id;
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public Course getCourse() {
        return course;
    }
    
    @Override
    public Iterable<Competitor> getCompetitors() {
        return Collections.unmodifiableSet(competitors);
    }
    
    @Override
    public byte[] getCompetitorMD5() {
        return raceCompetitorsMD5Hash.getMd5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID();
    }

    @Override
    public Competitor getCompetitorById(Serializable competitorID) {
        return competitorsById.get(competitorID);
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public Boat getBoatOfCompetitor(Competitor competitor) {
        return competitorBoats.get(competitor);
    }
}
