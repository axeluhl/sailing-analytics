package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class RaceDefinitionImpl extends NamedImpl implements RaceDefinition {
    private static final long serialVersionUID = -1900955198751393727L;
    private final Course course;
    private final Iterable<Competitor> competitors;
    private final BoatClass boatClass;
    private final Serializable id;
    
    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors) {
        this(name, course, boatClass, competitors, /* use name as default ID */ name);
    }

    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<? extends Competitor> competitors, Serializable id) {
        super(name);
        assert name != null;
        
        this.course = course;
        Set<Competitor> competitorsAsLinkedHashSet = new LinkedHashSet<Competitor>();
        for (Competitor competitor : competitors) {
            competitorsAsLinkedHashSet.add(competitor);
        }
        this.competitors = competitorsAsLinkedHashSet;
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
        return competitors;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

}
