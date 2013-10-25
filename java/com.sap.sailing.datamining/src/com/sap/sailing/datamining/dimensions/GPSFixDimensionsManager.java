package com.sap.sailing.datamining.dimensions;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.impl.AbstractDimension;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.domain.common.LegType;

public class GPSFixDimensionsManager {

    private GPSFixDimensionsManager() { }

    public final static Dimension<GPSFixWithContext, String> RegattaName = new AbstractDimension<GPSFixWithContext, String>("Regatta") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getRegattaName();
        }
    };

    public final static Dimension<GPSFixWithContext, String> RaceName = new AbstractDimension<GPSFixWithContext, String>("Race") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getRaceName();
        }
    };

    public final static Dimension<GPSFixWithContext, Integer> LegNumber = new AbstractDimension<GPSFixWithContext, Integer>("Leg Number") {
        @Override
        public Integer getDimensionValueFrom(GPSFixWithContext data) {
            return data.getLegNumber();
        }
    };

    public final static Dimension<GPSFixWithContext, String> CourseAreaName = new AbstractDimension<GPSFixWithContext, String>("Course Area") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCourseAreaName();
        }
    };

    public final static Dimension<GPSFixWithContext, String> FleetName = new AbstractDimension<GPSFixWithContext, String>("Fleet") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getFleetName();
        }
    };

    public final static Dimension<GPSFixWithContext, String> BoatClassName = new AbstractDimension<GPSFixWithContext, String>("Boat Class") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getBoatClassName();
        }
    };

    public final static Dimension<GPSFixWithContext, Integer> Year = new AbstractDimension<GPSFixWithContext, Integer>("Year") {
        @Override
        public Integer getDimensionValueFrom(GPSFixWithContext data) {
            return data.getYear();
        }
    };

    public final static Dimension<GPSFixWithContext, LegType> LegType = new AbstractDimension<GPSFixWithContext, LegType>("Leg Type") {
        @Override
        public LegType getDimensionValueFrom(GPSFixWithContext data) {
            return data.getLegType();
        }
    };

    public final static Dimension<GPSFixWithContext, String> CompetitorName = new AbstractDimension<GPSFixWithContext, String>("Competitor") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCompetitorName();
        }
    };

    public final static Dimension<GPSFixWithContext, String> SailID = new AbstractDimension<GPSFixWithContext, String>("Sail ID") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCompetitorSailID();
        }
    };

    public final static Dimension<GPSFixWithContext, String> Nationality = new AbstractDimension<GPSFixWithContext, String>("Nationality") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCompetitorNationality();
        }
    };

    //TODO after new clusters
    public final static Dimension<GPSFixWithContext, String> WindStrength = new AbstractDimension<GPSFixWithContext, String>("Wind Strength") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getWindStrength().getName();
        }
    };

    /**
     * @return The dimension for the given dimension type. Throws an exception, if the used <code>ValueType</code> doesn't match the <code>ValueType</code> of the returning dimension.
     */
    @SuppressWarnings("unchecked")
    public static <ValueType> Dimension<GPSFixWithContext, ValueType> getDimensionFor(SharedDimension dimension) {
        switch (dimension) {
        case BoatClassName:
            return (Dimension<GPSFixWithContext, ValueType>) BoatClassName;
        case CompetitorName:
            return (Dimension<GPSFixWithContext, ValueType>) CompetitorName;
        case CourseAreaName:
            return (Dimension<GPSFixWithContext, ValueType>) CourseAreaName;
        case FleetName:
            return (Dimension<GPSFixWithContext, ValueType>) FleetName;
        case LegNumber:
            return (Dimension<GPSFixWithContext, ValueType>) LegNumber;
        case LegType:
            return (Dimension<GPSFixWithContext, ValueType>) LegType;
        case Nationality:
            return (Dimension<GPSFixWithContext, ValueType>) Nationality;
        case RaceName:
            return (Dimension<GPSFixWithContext, ValueType>) RaceName;
        case RegattaName:
            return (Dimension<GPSFixWithContext, ValueType>) RegattaName;
        case SailID:
            return (Dimension<GPSFixWithContext, ValueType>) SailID;
        case WindStrength:
            return (Dimension<GPSFixWithContext, ValueType>) WindStrength;
        case Year:
            return (Dimension<GPSFixWithContext, ValueType>) Year;
        }
        throw new IllegalArgumentException("Not yet implemented for the given dimension: "
                + dimension.toString());
    }

}