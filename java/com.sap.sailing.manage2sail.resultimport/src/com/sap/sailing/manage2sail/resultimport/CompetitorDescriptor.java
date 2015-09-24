package com.sap.sailing.manage2sail.resultimport;

import com.sap.sailing.domain.base.Person;
import com.sap.sse.common.CountryCode;

public class CompetitorDescriptor {
    private String eventName;
    private String regattaName;
    private String raceName;
    private String fleetName;
    private String sailNumber;
    private String competitorName;
    private CountryCode countryCode;
    private Iterable<Person> persons;

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

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public void setRegattaName(String regattaName) {
        this.regattaName = regattaName;
    }

    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public String getFleetName() {
        return fleetName;
    }

    public void setFleetName(String fleetName) {
        this.fleetName = fleetName;
    }

    public String getSailNumber() {
        return sailNumber;
    }

    public void setSailNumber(String sailNumber) {
        this.sailNumber = sailNumber;
    }

    public String getCompetitorName() {
        return competitorName;
    }

    public void setCompetitorName(String competitorName) {
        this.competitorName = competitorName;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(CountryCode threeLetterIOCCountryCode) {
        this.countryCode = threeLetterIOCCountryCode;
    }

    public Iterable<Person> getPersons() {
        return persons;
    }

    public void setPersons(Iterable<Person> persons) {
        this.persons = persons;
    }

    @Override
    public String toString() {
        return ""+countryCode+" "+sailNumber+" "+getCompetitorName()+" at event: "+eventName+", regatta: "+regattaName+", race: "+raceName+
                ", fleet: "+fleetName+", "+persons;
    }
}
