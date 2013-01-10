package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class CourseAreaImpl extends NamedImpl implements CourseArea {
    private static final long serialVersionUID = 5912385360170509150L;
    
    private ArrayList<RaceDefinition> races;

    public CourseAreaImpl(String name) {
        super(name);
        this.races = new ArrayList<RaceDefinition>();
    }

	@Override
	public void addRace(RaceDefinition race) {
		synchronized (races) {
			races.add(race);
		}
	}

	@Override
	public Iterable<RaceDefinition> getRaces() {
		return Collections.unmodifiableList(races);
	}
}
