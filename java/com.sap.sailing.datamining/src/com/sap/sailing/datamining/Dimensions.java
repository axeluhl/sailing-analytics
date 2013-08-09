package com.sap.sailing.datamining;

import com.sap.sailing.datamining.impl.AbstractDimension;

public class Dimensions {

    private Dimensions() { }

    public static class GPSFix {

        private GPSFix() { }

        public static Dimension<GPSFixWithContext, String> RegattaName = new AbstractDimension<GPSFixWithContext, String>("Regatta") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getRegattaName();
            }
        };

        public static Dimension<GPSFixWithContext, String> RaceName = new AbstractDimension<GPSFixWithContext, String>("Race") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getRaceName();
            }
        };

        public static Dimension<GPSFixWithContext, String> LegNumber = new AbstractDimension<GPSFixWithContext, String>("Leg Number") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getLegNumber() + "";
            }
        };

        public static Dimension<GPSFixWithContext, String> CourseArea = new AbstractDimension<GPSFixWithContext, String>("Course Area") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getCourseAreaName();
            }
        };

        public static Dimension<GPSFixWithContext, String> Fleet = new AbstractDimension<GPSFixWithContext, String>("Fleet") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getFleetName();
            }
        };

        public static Dimension<GPSFixWithContext, String> BoatClassName = new AbstractDimension<GPSFixWithContext, String>("Boat Class") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getBoatClassName();
            }
        };

        public static Dimension<GPSFixWithContext, String> Year = new AbstractDimension<GPSFixWithContext, String>("Year") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getYear() + "";
            }
        };

        public static Dimension<GPSFixWithContext, String> LegType = new AbstractDimension<GPSFixWithContext, String>("Leg Type") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getLegType().toString();
            }
        };

        public static Dimension<GPSFixWithContext, String> CompetitorName = new AbstractDimension<GPSFixWithContext, String>("Competitor") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getCompetitorName();
            }
        };

        public static Dimension<GPSFixWithContext, String> SailID = new AbstractDimension<GPSFixWithContext, String>("Sail ID") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getCompetitorSailID();
            }
        };

        public static Dimension<GPSFixWithContext, String> Nationality = new AbstractDimension<GPSFixWithContext, String>("Nationality") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getCompetitorNationality();
            }
        };

        public static Dimension<GPSFixWithContext, String> WindStrength = new AbstractDimension<GPSFixWithContext, String>("Wind Strength") {
            @Override
            public String getDimensionValueFrom(GPSFixWithContext data) {
                return data.getWindStrength().getName();
            }
        };

    }

}
