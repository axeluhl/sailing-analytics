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

}
