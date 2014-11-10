package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sse.common.impl.NamedImpl;

public class RaceDefinitionImpl extends NamedImpl implements RaceDefinition {
    private static final long serialVersionUID = -1900955198751393727L;
    private final Course course;
    private final LinkedHashMap<Serializable, Competitor> competitorsById;
    private final BoatClass boatClass;
    private final Serializable id;
    
    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors) {
        this(name, course, boatClass, competitors, /* use name as default ID */ name);
    }

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors, Serializable id) {
        super(name);
        assert name != null;
        
        this.course = course;
        this.competitorsById = new LinkedHashMap<>();
        for (Competitor competitor : competitors) {
            Competitor competitorWithEqualID = competitorsById.put(competitor.getId(), competitor);
            if (competitorWithEqualID != null && competitorWithEqualID != competitor) {
                throw new IllegalArgumentException("Two distinct competitors with equal ID "+competitor.getId()+" are not allowed within the single race "+name);
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
        return competitorsById.values();
    }

    @Override
    public Competitor getCompetitorById(Serializable competitorID) {
        return competitorsById.get(competitorID);
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

}
