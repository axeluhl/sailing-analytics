package com.sap.sailing.datamining.shared;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum DimensionIdentifier {
    RegattaName, BoatClassName, CourseAreaName, FleetName, RaceName, LegType, LegNumber, Nationality, CompetitorName, SailID, Year;
    
    public static class OrdinalComparator implements Comparator<DimensionIdentifier> {
        private static final List<DimensionIdentifier> dimensions = Arrays.asList(DimensionIdentifier.values());

        @Override
        public int compare(DimensionIdentifier dimension1, DimensionIdentifier dimension2) {
            Integer ordinal1 = dimensions.indexOf(dimension1);
            Integer ordinal2 = dimensions.indexOf(dimension2);
            return ordinal1.compareTo(ordinal2);
        }
        
    }
}