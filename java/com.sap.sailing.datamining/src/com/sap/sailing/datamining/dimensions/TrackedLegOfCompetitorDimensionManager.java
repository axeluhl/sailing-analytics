package com.sap.sailing.datamining.dimensions;

import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.domain.common.LegType;
import com.sap.sse.datamining.data.deprecated.Dimension;
import com.sap.sse.datamining.impl.data.deprecated.AbstractDimension;

public final class TrackedLegOfCompetitorDimensionManager implements DimensionManager<TrackedLegOfCompetitorWithContext> {

    public TrackedLegOfCompetitorDimensionManager() { }

    /**
     * @return The dimension for the given dimension type. Throws an exception, if the used <code>ValueType</code> doesn't match the <code>ValueType</code> of the returning dimension.
     */
    public Dimension<TrackedLegOfCompetitorWithContext, ?> getDimensionFor(DimensionIdentifier dimension) {
        switch (dimension) {
        case BoatClassName:
            return BoatClassNameDimension;
        case CompetitorName:
            return CompetitorNameDimension;
        case CourseAreaName:
            return CourseAreaNameDimension;
        case FleetName:
            return FleetNameDimension;
        case LegNumber:
            return LegNumberDimension;
        case LegType:
            return LegTypeDimension;
        case Nationality:
            return NationalityDimension;
        case RaceName:
            return RaceNameDimension;
        case RegattaName:
            return RegattaNameDimension;
        case SailID:
            return SailIDDimension;
        case WindStrength:
            return WindStrengthDimension;
        case Year:
            return YearDimension;
        }
        throw new IllegalArgumentException("Not yet implemented for the given dimension: "
                + dimension.toString());
    }

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> RegattaNameDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Regatta") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getRegattaName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> RaceNameDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Race") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getRaceName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, Integer> LegNumberDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, Integer>("Leg Number") {
        @Override
        public Integer getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getLegNumber();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> CourseAreaNameDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Course Area") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getCourseAreaName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> FleetNameDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Fleet") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getFleetName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> BoatClassNameDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Boat Class") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getBoatClassName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, Integer> YearDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, Integer>("Year") {
        @Override
        public Integer getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getYear();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, LegType> LegTypeDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, LegType>("Leg Type") {
        @Override
        public LegType getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getLegType();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> CompetitorNameDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Competitor") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getCompetitorName();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> SailIDDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Sail ID") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getCompetitorSailID();
        }
    };

    public final static Dimension<TrackedLegOfCompetitorWithContext, String> NationalityDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Nationality") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getCompetitorNationality();
        }
    };

    //TODO after new clusters
    public final static Dimension<TrackedLegOfCompetitorWithContext, String> WindStrengthDimension = new AbstractDimension<TrackedLegOfCompetitorWithContext, String>("Wind Strength") {
        @Override
        public String getDimensionValueFrom(TrackedLegOfCompetitorWithContext data) {
            return data.getWindStrength().getName();
        }
    };

}