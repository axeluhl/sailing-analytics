package com.sap.sailing.domain.base.impl;

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
    
    public RaceDefinitionImpl(String name, Course course, BoatClass boatClass, Iterable<Competitor> competitors) {
        super(name);
        this.course = course;
        this.competitors = competitors;
        this.boatClass = boatClass;
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
