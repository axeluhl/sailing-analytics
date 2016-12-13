package com.sap.sailing.competitorimport;

import com.sap.sailing.domain.base.Person;
import com.sap.sse.common.CountryCode;


public class CompetitorDescriptor {
    private final String eventName;
    private final String regattaName;
    private final String raceName;
    private final String fleetName;
    private final String sailNumber;
    private final String competitorName;
    private final CountryCode countryCode;
    private final Iterable<Person> persons;

    public CompetitorDescriptor(String eventName, String regattaName, String raceName, String fleetName,
            String sailNumber, String competitorName, CountryCode countryCode, Iterable<Person> persons) {
        super();
        this.eventName = eventName;
        this.regattaName = regattaName;
        this.raceName = raceName;
        this.fleetName = fleetName;
        this.sailNumber = sailNumber;
        this.competitorName = competitorName;
        this.countryCode = countryCode;
        this.persons = persons;
    }

    public String getEventName() {
        return eventName;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public String getRaceName() {
        return raceName;
    }

    public String getFleetName() {
        return fleetName;
    }

    public String getSailNumber() {
        return sailNumber;
    }

    public String getCompetitorName() {
        return competitorName;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public Iterable<Person> getPersons() {
        return persons;
    }

    @Override
    public String toString() {
        return ""+countryCode+" "+sailNumber+" "+getCompetitorName()+" at event: "+eventName+", regatta: "+regattaName+", race: "+raceName+
                ", fleet: "+fleetName+", "+persons;
    }
}
