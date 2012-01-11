package com.sap.sailing.geocoding.impl;

import com.sap.sailing.domain.common.DegreePosition;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.geocoding.Placemark;

public class PlacemarkImpl implements Placemark {
    
    private String name;
    private String countryCode;
    private Position position;
    private String type;
    private long population;
    
    public PlacemarkImpl(String name, String countryCode, Position position, String type, long population) {
        this.name = name;
        this.countryCode = countryCode;
        this.position = position;
        this.type = type;
        this.population = population;
    }
    
    public PlacemarkImpl(String name, String countryCode, Position position, String type) {
        this(name, countryCode, position, type, 0);
    }

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }
    
    public Position getPosition() {
        return position;
    }

    public String getType() {
        return type;
    }

    public long getPopulation() {
        return population;
    }


    @Override
    public Distance distanceFrom(Position p) {
        return position.getDistance(p);
    }
    @Override
    public Distance distanceFrom(double latDeg, double lngDeg) {
        Position p = new DegreePosition(latDeg, lngDeg);
        return distanceFrom(p);
    }

    @Override
    public String getCountryName() {
        // TODO Get country name from CountryCodeFactoryImpl with countryCode
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((countryCode == null) ? 0 : countryCode.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (population ^ (population >>> 32));
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlacemarkImpl other = (PlacemarkImpl) obj;
        if (countryCode == null) {
            if (other.countryCode != null)
                return false;
        } else if (!countryCode.equals(other.countryCode))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (population != other.population)
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
