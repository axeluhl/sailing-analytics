package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RaceCompetitorIdsAsStringWithMD5Hash;
import com.sap.sse.common.impl.NamedImpl;

public class RaceDefinitionImpl extends NamedImpl implements RaceDefinition {
    private static final long serialVersionUID = -1900955198751393727L;
    private final Course course;
    private final HashMap<Serializable, Competitor> competitorsByCompetitorId;
    private final HashMap<Serializable, Boat> boatsByCompetitorId;
    private final BoatClass boatClass;
    private final Serializable id;
    private final RaceCompetitorIdsAsStringWithMD5Hash raceCompetitorsMD5Hash;
    private final Map<Competitor, Boat> competitorsAndTheirBoats;

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass) {
        this(name, course, boatClass, Collections.emptyMap());
    }

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Map<Competitor, Boat> competitorsAndTheirBoats) {
        this(name, course, boatClass, competitorsAndTheirBoats, /* use name as default ID */ name);
    }

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Map<Competitor, Boat> competitorsAndTheirBoats, Serializable id) {
        super(name);
        assert name != null;
        this.boatClass = boatClass;
        this.course = course;
        this.competitorsAndTheirBoats = new HashMap<>(competitorsAndTheirBoats);
        this.id = id;

        final Set<String> idsOfCompetitorsAsString = new HashSet<>();
        this.competitorsByCompetitorId = new HashMap<>();
        this.boatsByCompetitorId = new HashMap<>();
        
        for (Entry<Competitor, Boat> competitorAndBoat : competitorsAndTheirBoats.entrySet()) {
            Competitor competitor = competitorAndBoat.getKey();            
            Competitor competitorWithEqualID = competitorsByCompetitorId.put(competitor.getId(), competitor);
            boatsByCompetitorId.put(competitor.getId(), competitorAndBoat.getValue());
            idsOfCompetitorsAsString.add(competitor.getId().toString());
            if (competitorWithEqualID != null && competitorWithEqualID != competitor) {
                throw new IllegalArgumentException("Two distinct competitors with equal ID "+competitor.getId()+" are not allowed within the single race "+name);
            }
        }
        try {
            raceCompetitorsMD5Hash = new RaceCompetitorIdsAsStringWithMD5Hash(idsOfCompetitorsAsString);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Internal error: issue with UTF8 or MD5 for encoding competitor IDs as MD5 hash", e);
        }
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
        return Collections.unmodifiableSet(competitorsAndTheirBoats.keySet());
    }

    @Override
    public Iterable<Boat> getBoats() {
        return Collections.unmodifiableCollection(competitorsAndTheirBoats.values());
    }

    @Override
    public Map<Competitor, Boat> getCompetitorsAndTheirBoats() {
        return Collections.unmodifiableMap(competitorsAndTheirBoats);
    }

    @Override
    public byte[] getCompetitorMD5() {
        return raceCompetitorsMD5Hash.getMd5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID();
    }

    @Override
    public Competitor getCompetitorById(Serializable competitorID) {
        return competitorsByCompetitorId.get(competitorID);
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public Boat getBoatOfCompetitor(Competitor competitor) {
        return competitorsAndTheirBoats.get(competitor);
    }
}
