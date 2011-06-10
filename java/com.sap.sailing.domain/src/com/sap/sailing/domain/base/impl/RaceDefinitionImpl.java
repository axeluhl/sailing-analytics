package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;

public class RaceDefinitionImpl extends NamedImpl implements RaceDefinition {
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

    @Override
    public void updateCourse(Course course) {
        // FIXME we learned by e-mail from Lasse (2011-06-04T20:38:00CET) that courses may change during a race; how to handle???
        // TODO implement RaceDefinitionImpl.updateCourse and a corresponding observer pattern updating the TrackedLeg[OfCompetitor] structures as well
    }

}
