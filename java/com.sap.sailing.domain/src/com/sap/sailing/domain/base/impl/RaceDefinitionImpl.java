package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.NamedImpl;

public class RaceDefinitionImpl extends NamedImpl implements RaceDefinition {
    private static final long serialVersionUID = -1900955198751393727L;
    private final Course course;
    private final LinkedHashMap<Serializable, Competitor> competitorsById;
    private final Set<Competitor> competitors;
    private final Map<Serializable, Boat> competitorBoats;
    private final BoatClass boatClass;
    private final Serializable id;

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors) {
        this(name, course, boatClass, competitors, Collections.emptyList());
    }

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors,
            Iterable<Pair<Competitor, Boat>> competitorsAndTheirBoats) {
        this(name, course, boatClass, competitors, competitorsAndTheirBoats, /* use name as default ID */ name);
    }

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors, Serializable id) {
        this(name, course, boatClass, competitors, /* per-race boats for competitors */ Collections.emptyList(), id);
    }
    
    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors, 
            Iterable<Pair<Competitor, Boat>> competitorsAndTheirBoats, Serializable id) {
        super(name);
        assert name != null;
        this.course = course;
        this.competitors = new HashSet<>();
        this.competitorsById = new LinkedHashMap<>();
        this.competitorBoats = new HashMap<>();
        for (Competitor competitor : competitors) {
            Competitor competitorWithEqualID = competitorsById.put(competitor.getId(), competitor);
            this.competitors.add(competitor);
            if (competitorWithEqualID != null && competitorWithEqualID != competitor) {
                throw new IllegalArgumentException("Two distinct competitors with equal ID "+competitor.getId()+" are not allowed within the single race "+name);
            }
        }
        for (Pair<Competitor, Boat> competitorAndBoat : competitorsAndTheirBoats) {
            Competitor competitor = competitorsById.get(competitorAndBoat.getA().getId());
            if (competitor != null && competitorAndBoat.getB() != null) {
                competitorBoats.put(competitor.getId(), competitorAndBoat.getB());
            }
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
    public Competitor getCompetitorById(Serializable competitorID) {
        return competitorsById.get(competitorID);
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public Boat getBoatOfCompetitorById(Serializable competitorID) {
        return competitorBoats.get(competitorID);
    }
}
