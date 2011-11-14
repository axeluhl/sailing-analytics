package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.Race;

public class DomainFactoryImpl implements DomainFactory {
    private final Map<String, Event> raceIDToEventCache;
    private final Map<String, Competitor> boatIDToCompetitorCache;
    private final Map<String, Nationality> nationalityCache;
    
    public DomainFactoryImpl() {
        raceIDToEventCache = new HashMap<String, Event>();
        boatIDToCompetitorCache = new HashMap<String, Competitor>();
        nationalityCache = new HashMap<String, Nationality>();
    }

    @Override
    public Event getOrCreateEvent(Race race) {
        Event result = raceIDToEventCache.get(race.getRaceID());
        if (result == null) {
            result = new EventImpl(race.getDescription(), null);
            raceIDToEventCache.put(race.getRaceID(), result);
        }
        return result;
    }
    
    @Override
    public Competitor getOrCreateCompetitor(com.sap.sailing.domain.swisstimingadapter.Competitor competitor) {
        Competitor result = boatIDToCompetitorCache.get(competitor.getBoatID());
        if (result == null) {
            Boat boat = new BoatImpl(competitor.getName(), null, competitor.getBoatID());
            List<Person> teamMembers = new ArrayList<Person>();
            for (String teamMemberName : competitor.getName().split("[-+&]")) {
                teamMembers.add(new PersonImpl(teamMemberName.trim(), getOrCreateNationality(competitor.getThreeLetterIOCCode()),
                        /* dateOfBirth */ null, teamMemberName.trim()));
            }
            Team team = new TeamImpl(competitor.getName(), teamMembers, /* coach */ null);
            result = new com.sap.sailing.domain.base.impl.CompetitorImpl(competitor.getBoatID(), competitor.getName(), team, boat);
            boatIDToCompetitorCache.put(competitor.getBoatID(), result);
        }
        return result;
    }

    @Override
    public Nationality getOrCreateNationality(String nationalityName) {
        synchronized (nationalityCache) {
            Nationality result = nationalityCache.get(nationalityName);
            if (result == null) {
                result = new NationalityImpl(nationalityName, nationalityName);
                nationalityCache.put(nationalityName, result);
            }
            return result;
        }
    }
}
