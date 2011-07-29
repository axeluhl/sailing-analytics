package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Team;

public class TeamImpl extends NamedImpl implements Team {
    private final Iterable<? extends Person> sailors;
    private final Person coach;
    
    public TeamImpl(String name, Iterable<? extends Person> sailors, Person coach) {
        super(name);
        this.sailors = sailors;
        this.coach = coach;
    }

    @Override
    public Iterable<? extends Person> getSailors() {
        return sailors;
    }
    
    @Override
    public Person getCoach() {
        return coach;
    }

    @Override
    public Nationality getNationality() {
        for (Person sailor : getSailors()) {
            if (sailor.getNationality() != null) {
                return sailor.getNationality();
            }
        }
        if (getCoach() != null) {
            return getCoach().getNationality();
        }
        return null;
    }

}
