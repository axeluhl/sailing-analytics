package com.sap.sailing.datamining.impl.gpsfix;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.impl.AbstractDimension;

public abstract class GPSFixDimension extends AbstractDimension<GPSFixWithContext> {

    private String name;

    public GPSFixDimension(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        GPSFixDimension other = (GPSFixDimension) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public static GPSFixDimension RegattaName = new GPSFixDimension("Regatta") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getRegattaName();
        }};
        
    public static GPSFixDimension RaceName = new GPSFixDimension("Race") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getRaceName();
        }};
        
    public static GPSFixDimension LegNumber = new GPSFixDimension("Leg Number") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getLegNumber() + "";
        }};
        
    public static GPSFixDimension CourseArea = new GPSFixDimension("Course Area") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCourseAreaName();
        }};
        
    public static GPSFixDimension Fleet = new GPSFixDimension("Fleet") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getFleetName();
        }};
        
    public static GPSFixDimension BoatClassName = new GPSFixDimension("Boat Class") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getBoatClassName();
        }};
        
    public static GPSFixDimension Year = new GPSFixDimension("Year") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getYear() + "";
        }};
        
    public static GPSFixDimension LegType = new GPSFixDimension("Leg Type") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getLegType().toString();
        }};
        
    public static GPSFixDimension CompetitorName = new GPSFixDimension("Competitor") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCompetitorName();
        }};
        
    public static GPSFixDimension SailID = new GPSFixDimension("Sail ID") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCompetitorSailID();
        }};
        
    public static GPSFixDimension Nationality = new GPSFixDimension("Nationality") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCompetitorNationality();
        }};
        
    public static GPSFixDimension WindStrength = new GPSFixDimension("Wind Strength") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getWindStrength().getName();
        }};

}
