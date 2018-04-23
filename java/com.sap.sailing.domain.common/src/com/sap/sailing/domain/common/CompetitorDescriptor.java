package com.sap.sailing.domain.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.common.dto.PersonDTO;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Named;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;


public class CompetitorDescriptor extends NamedImpl implements Named {
    private static final long serialVersionUID = -6645726854552711646L;
    
    private final UUID competitorUUID;
    private final UUID boatUUID;
    private final String shortName;
    private final String eventName;
    private final String regattaName;
    private final String boatClassName;
    private final String boatName;
    private final String teamName;
    private final String raceName;
    private final String fleetName;
    private final String sailNumber;
    private final CountryCode countryCode;
    private final List<PersonDTO> persons;
    private final Double timeOnTimeFactor;
    private final Duration timeOnDistanceAllowancePerNauticalMile;

    public CompetitorDescriptor(String eventName, String regattaName, String raceName, String fleetName,
            UUID competitorUUID, String fullName, String shortName, String teamName, Iterable<PersonDTO> persons,
            CountryCode countryCode, Double timeOnTimeFactor,
            Duration timeOnDistanceAllowancePerNauticalMile, UUID boatUUID, String boatName, String boatClassName, String sailNumber) {
        super(fullName);
        this.shortName = shortName;
        this.eventName = eventName;
        this.regattaName = regattaName;
        this.raceName = raceName;
        this.fleetName = fleetName;
        this.competitorUUID = competitorUUID; 
        this.teamName = teamName;
        this.countryCode = countryCode;
        this.persons = new ArrayList<>();
        Util.addAll(persons, this.persons);
        this.timeOnTimeFactor = timeOnTimeFactor;
        this.timeOnDistanceAllowancePerNauticalMile = timeOnDistanceAllowancePerNauticalMile;
        this.boatUUID = boatUUID;
        this.boatName = boatName;
        this.boatClassName = boatClassName;
        this.sailNumber = sailNumber;
    }

    public String getEventName() {
        return eventName;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public String getBoatClassName() {
        return boatClassName;
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

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public Iterable<PersonDTO> getPersons() {
        return persons;
    }

    public Double getTimeOnTimeFactor() {
        return timeOnTimeFactor;
    }

    public String getBoatName() {
        return boatName;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getShortName() {
        return shortName;
    }

    public Duration getTimeOnDistanceAllowancePerNauticalMile() {
        return timeOnDistanceAllowancePerNauticalMile;
    }

    public UUID getCompetitorUUID() {
        return competitorUUID;
    }

    public UUID getBoatUUID() {
        return boatUUID;
    }
    
    @Override
    public String toString() {
        return "CompetitorDescriptor [eventName=" + eventName + ", regattaName=" + regattaName + ", boatClassName="
                + boatClassName + ", boatName=" + boatName + ", teamName=" + teamName + ", raceName=" + raceName
                + ", fleetName=" + fleetName + ", sailNumber=" + sailNumber + ", name=" + super.getName()
                + ", countryCode="
                + countryCode + ", persons=" + persons + ", timeOnTimeFactor=" + timeOnTimeFactor
                + ", timeOnDistanceAllowancePerNauticalMile=" + timeOnDistanceAllowancePerNauticalMile + "]";
    }
}
