package com.sap.sailing.datamining.dimensions;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.impl.AbstractDimension;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.domain.common.LegType;

public final class GPSFixDimensionManager implements DimensionManager<GPSFixWithContext> {

    public GPSFixDimensionManager() { }

    /**
     * @return The dimension for the given dimension type. Throws an exception, if the used <code>ValueType</code> doesn't match the <code>ValueType</code> of the returning dimension.
     */
    @Override
    public Dimension<GPSFixWithContext, ?> getDimensionFor(SharedDimension dimension) {
        switch (dimension) {
        case BoatClassName:
            return (Dimension<GPSFixWithContext, ?>) BoatClassNameDimension;
        case CompetitorName:
            return (Dimension<GPSFixWithContext, ?>) CompetitorNameDimension;
        case CourseAreaName:
            return (Dimension<GPSFixWithContext, ?>) CourseAreaNameDimension;
        case FleetName:
            return (Dimension<GPSFixWithContext, ?>) FleetNameDimension;
        case LegNumber:
            return (Dimension<GPSFixWithContext, ?>) LegNumberDimension;
        case LegType:
            return (Dimension<GPSFixWithContext, ?>) LegTypeDimension;
        case Nationality:
            return (Dimension<GPSFixWithContext, ?>) NationalityDimension;
        case RaceName:
            return (Dimension<GPSFixWithContext, ?>) RaceNameDimension;
        case RegattaName:
            return (Dimension<GPSFixWithContext, ?>) RegattaNameDimension;
        case SailID:
            return (Dimension<GPSFixWithContext, ?>) SailIDDimension;
        case WindStrength:
            return (Dimension<GPSFixWithContext, ?>) WindStrengthDimension;
        case Year:
            return (Dimension<GPSFixWithContext, ?>) YearDimension;
        }
        throw new IllegalArgumentException("Not yet implemented for the given dimension: "
                + dimension.toString());
    }

    public final static Dimension<GPSFixWithContext, String> RegattaNameDimension = new AbstractDimension<GPSFixWithContext, String>("Regatta") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getRegattaName();
        }
    };

    public final static Dimension<GPSFixWithContext, String> RaceNameDimension = new AbstractDimension<GPSFixWithContext, String>("Race") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getRaceName();
        }
    };

    public final static Dimension<GPSFixWithContext, Integer> LegNumberDimension = new AbstractDimension<GPSFixWithContext, Integer>("Leg Number") {
        @Override
        public Integer getDimensionValueFrom(GPSFixWithContext data) {
            return data.getLegNumber();
        }
    };

    public final static Dimension<GPSFixWithContext, String> CourseAreaNameDimension = new AbstractDimension<GPSFixWithContext, String>("Course Area") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCourseAreaName();
        }
    };

    public final static Dimension<GPSFixWithContext, String> FleetNameDimension = new AbstractDimension<GPSFixWithContext, String>("Fleet") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getFleetName();
        }
    };

    public final static Dimension<GPSFixWithContext, String> BoatClassNameDimension = new AbstractDimension<GPSFixWithContext, String>("Boat Class") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getBoatClassName();
        }
    };

    public final static Dimension<GPSFixWithContext, Integer> YearDimension = new AbstractDimension<GPSFixWithContext, Integer>("Year") {
        @Override
        public Integer getDimensionValueFrom(GPSFixWithContext data) {
            return data.getYear();
        }
    };

    public final static Dimension<GPSFixWithContext, LegType> LegTypeDimension = new AbstractDimension<GPSFixWithContext, LegType>("Leg Type") {
        @Override
        public LegType getDimensionValueFrom(GPSFixWithContext data) {
            return data.getLegType();
        }
    };

    public final static Dimension<GPSFixWithContext, String> CompetitorNameDimension = new AbstractDimension<GPSFixWithContext, String>("Competitor") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCompetitorName();
        }
    };

    public final static Dimension<GPSFixWithContext, String> SailIDDimension = new AbstractDimension<GPSFixWithContext, String>("Sail ID") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCompetitorSailID();
        }
    };

    public final static Dimension<GPSFixWithContext, String> NationalityDimension = new AbstractDimension<GPSFixWithContext, String>("Nationality") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getCompetitorNationality();
        }
    };

    //TODO after new clusters
    public final static Dimension<GPSFixWithContext, String> WindStrengthDimension = new AbstractDimension<GPSFixWithContext, String>("Wind Strength") {
        @Override
        public String getDimensionValueFrom(GPSFixWithContext data) {
            return data.getWindStrength().getName();
        }
    };

}