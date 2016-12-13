package com.sap.sailing.domain.common.dto;

import java.util.Objects;
import java.util.Set;

public class CompetitorDescriptorDTO extends NamedDTO {
    private static final long serialVersionUID = 4855053239521181610L;
    private String eventName;
    private String regattaName;
    private String raceName;
    private String fleetName;
    private String sailNumber;

    private String countryName;
    private String twoLetterIsoCountryCode;
    private String threeLetterIocCountryCode;

    private Set<PersonDTO> persons;

    CompetitorDescriptorDTO() {} // for GWT de-serialization only

    public CompetitorDescriptorDTO(String eventName, String regattaName, String raceName, String fleetName,
            String sailNumber, String competitorName, String countryName, String twoLetterIsoCountryCode,
            String threeLetterIocCountryCode, Set<PersonDTO> persons) {
        super(competitorName);
        this.eventName = eventName;
        this.regattaName = regattaName;
        this.raceName = raceName;
        this.fleetName = fleetName;
        this.sailNumber = sailNumber;
        this.countryName = countryName;
        this.twoLetterIsoCountryCode = twoLetterIsoCountryCode;
        this.threeLetterIocCountryCode = threeLetterIocCountryCode;
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

    public Iterable<PersonDTO> getPersons() {
        return persons;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getTwoLetterIsoCountryCode() {
        return twoLetterIsoCountryCode;
    }

    public String getThreeLetterIocCountryCode() {
        return threeLetterIocCountryCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(eventName, regattaName, raceName, fleetName, sailNumber, persons,
                countryName, twoLetterIsoCountryCode, threeLetterIocCountryCode);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CompetitorDescriptorDTO other = (CompetitorDescriptorDTO) obj;
        return Objects.equals(countryName, other.countryName) && Objects.equals(eventName, other.eventName)
                && Objects.equals(fleetName, other.fleetName) && Objects.deepEquals(persons, other.persons)
                && Objects.equals(raceName, other.raceName) && Objects.equals(regattaName, other.regattaName)
                && Objects.equals(sailNumber, other.sailNumber)
                && Objects.equals(threeLetterIocCountryCode, other.threeLetterIocCountryCode)
                && Objects.equals(twoLetterIsoCountryCode, other.twoLetterIsoCountryCode);
    }
}
