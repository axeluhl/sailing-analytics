package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PlacemarkDTO extends NamedDTO implements IsSerializable {

    private String countryCode;
    private PositionDTO position;
    private long population;
    
    /**
     * Constructor for serialization.
     */
    PlacemarkDTO() {}

    public PlacemarkDTO(String name, String countryCode, PositionDTO position, long population) {
        super(name);
        this.countryCode = countryCode;
        this.position = position;
        this.population = population;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public PositionDTO getPosition() {
        return position;
    }

    public void setPosition(PositionDTO position) {
        this.position = position;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }
    
    /**
     * @return The placemark as string with the format '<code>countryCode</code> , <code>name</code>'
     */
    public String asString() {
        return countryCode + ", " + name;
    }
    
    @Override
    public String toString() {
        return countryCode + ", " + name + ", " + population + ", " + position.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((countryCode == null) ? 0 : countryCode.hashCode());
        result = prime * result + (int) (population ^ (population >>> 32));
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlacemarkDTO other = (PlacemarkDTO) obj;
        if (countryCode == null) {
            if (other.countryCode != null)
                return false;
        } else if (!countryCode.equals(other.countryCode))
            return false;
        return true;
    }
    
}
