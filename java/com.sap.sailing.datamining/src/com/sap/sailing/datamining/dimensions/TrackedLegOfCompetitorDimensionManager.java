package com.sap.sailing.datamining.dimensions;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.AbstractDimension;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.domain.common.LegType;

public final class TrackedLegOfCompetitorDimensionManager implements DimensionManager<TrackedLegOfCompetitorWithContext> {

    public TrackedLegOfCompetitorDimensionManager() { }

    /**
     * @return The dimension for the given dimension type. Throws an exception, if the used <code>ValueType</code> doesn't match the <code>ValueType</code> of the returning dimension.
     */
    public Dimension<TrackedLegOfCompetitorWithContext, ?> getDimensionFor(SharedDimension dimension) {
        switch (dimension) {
        case BoatClassName:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) BoatClassName;
        case CompetitorName:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) CompetitorName;
        case CourseAreaName:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) CourseAreaName;
        case FleetName:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) FleetName;
        case LegNumber:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) LegNumber;
        case LegType:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) LegType;
        case Nationality:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) Nationality;
        case RaceName:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) RaceName;
        case RegattaName:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) RegattaName;
        case SailID:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) SailID;
        case WindStrength:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) WindStrength;
        case Year:
            return (Dimension<TrackedLegOfCompetitorWithContext, ?>) Year;
        }
        throw new IllegalArgumentException("Not yet implemented for the given dimension: "
                + dimension.toString());
    }

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> RegattaName = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Regatta") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getRegattaName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> RaceName = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Race") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getRaceName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, Integer> LegNumber = new AbstractDimension<TrackedLegOfCompetitorWithContext, Integer>("Leg Number") {
        @Override
        public Integer getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getLegNumber();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> CourseAreaName = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Course Area") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getCourseAreaName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> FleetName = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Fleet") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getFleetName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> BoatClassName = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Boat Class") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getBoatClassName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, Integer> Year = new AbstractDimension<TrackedLegOfCompetitorWithContext, Integer>("Year") {
        @Override
        public Integer getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getYear();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, LegType> LegType = new AbstractDimension<TrackedLegOfCompetitorWithContext, LegType>("Leg Type") {
        @Override
        public LegType getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getLegType();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> CompetitorName = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Competitor") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getCompetitorName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> SailID = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Sail ID") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getCompetitorSailID();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> Nationality = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Nationality") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getCompetitorNationality();
        }
    };

    //TODO after new clusters
    public final static Dimension<TrackedLegOfCompetitorWithContext, String> WindStrength = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Wind Strength") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getWindStrength().getName();
        }
    };

}