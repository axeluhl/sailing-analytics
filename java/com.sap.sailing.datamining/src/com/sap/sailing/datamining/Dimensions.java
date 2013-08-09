package com.sap.sailing.datamining;

import com.sap.sailing.datamining.impl.AbstractDimension;

public class Dimensions {

    private Dimensions() { }

    public static class GPSFix {

        private GPSFix() { }

        public static Dimension<GPSFixWithContext> RegattaName = new AbstractDimension<GPSFixWithContext>("Regatta") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getRegattaName();
            }
        };

        public static Dimension<GPSFixWithContext> RaceName = new AbstractDimension<GPSFixWithContext>("Race") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getRaceName();
            }
        };

        public static Dimension<GPSFixWithContext> LegNumber = new AbstractDimension<GPSFixWithContext>("Leg Number") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getLegNumber() + "";
            }
        };

        public static Dimension<GPSFixWithContext> CourseArea = new AbstractDimension<GPSFixWithContext>("Course Area") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getCourseAreaName();
            }
        };

        public static Dimension<GPSFixWithContext> Fleet = new AbstractDimension<GPSFixWithContext>("Fleet") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getFleetName();
            }
        };

        public static Dimension<GPSFixWithContext> BoatClassName = new AbstractDimension<GPSFixWithContext>("Boat Class") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getBoatClassName();
            }
        };

        public static Dimension<GPSFixWithContext> Year = new AbstractDimension<GPSFixWithContext>("Year") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getYear() + "";
            }
        };

        public static Dimension<GPSFixWithContext> LegType = new AbstractDimension<GPSFixWithContext>("Leg Type") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getLegType().toString();
            }
        };

        public static Dimension<GPSFixWithContext> CompetitorName = new AbstractDimension<GPSFixWithContext>("Competitor") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getCompetitorName();
            }
        };

        public static Dimension<GPSFixWithContext> SailID = new AbstractDimension<GPSFixWithContext>("Sail ID") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getCompetitorSailID();
            }
        };

        public static Dimension<GPSFixWithContext> Nationality = new AbstractDimension<GPSFixWithContext>("Nationality") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getCompetitorNationality();
            }
        };

        public static Dimension<GPSFixWithContext> WindStrength = new AbstractDimension<GPSFixWithContext>("Wind Strength") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getWindStrength().getName();
            }
        };

    }

}
