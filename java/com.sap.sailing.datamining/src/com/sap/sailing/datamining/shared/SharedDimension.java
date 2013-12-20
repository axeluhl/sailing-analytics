package com.sap.sailing.datamining.shared;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum SharedDimension {
    RegattaName, BoatClassName, CourseAreaName, FleetName, RaceName, LegType, LegNumber, Nationality, CompetitorName, SailID, WindStrength, Year;
    
    public static class OrdinalComparator implements Comparator<SharedDimension> {
        private static final List<SharedDimension> dimensions = Arrays.asList(SharedDimension.values());

        @Override
        public int compare(SharedDimension dimension1, SharedDimension dimension2) {
            Integer ordinal1 = dimensions.indexOf(dimension1);
            Integer ordinal2 = dimensions.indexOf(dimension2);
            return ordinal1.compareTo(ordinal2);
        }
        
    }
}