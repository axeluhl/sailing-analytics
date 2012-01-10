package com.sap.sailing.geocoding.impl;

import com.sap.sailing.geocoding.Placemark;

public class PlacemarkImpl implements Placemark {
    
    private String name;
    private String countryCode;
    private double latDeg;
    private double lngDeg;
    private String type;
    private long population;
    
    public PlacemarkImpl(String name, String countryCode, double latDeg, double lngDeg, String type, long population) {
        this.name = name;
        this.countryCode = countryCode;
        this.latDeg = latDeg;
        this.lngDeg = lngDeg;
        this.type = type;
        this.population = population;
    }
    
    public PlacemarkImpl(String name, String countryCode, double latDeg, double lngDeg, String type) {
        this(name, countryCode, latDeg, lngDeg, type, 0);
    }

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getLatDeg() {
        return latDeg;
    }

    public double getLngDeg() {
        return lngDeg;
    }

    public String getType() {
        return type;
    }

    public long getPopulation() {
        return population;
    }

    @Override
    public float distanceFrom(double latDeg, double lngDeg) {
        // TODO Auto-generated method stub
        return 0;
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
        long temp;
        temp = Double.doubleToLongBits(latDeg);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lngDeg);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (population ^ (population >>> 32));
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
        if (Double.doubleToLongBits(latDeg) != Double.doubleToLongBits(other.latDeg))
            return false;
        if (Double.doubleToLongBits(lngDeg) != Double.doubleToLongBits(other.lngDeg))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (population != other.population)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
