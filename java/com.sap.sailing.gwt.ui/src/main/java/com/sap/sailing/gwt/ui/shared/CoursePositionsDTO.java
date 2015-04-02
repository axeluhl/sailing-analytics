package com.sap.sailing.gwt.ui.shared;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.impl.DegreePosition;

public class CoursePositionsDTO implements IsSerializable {
    public List<DegreePosition> waypointPositions;
    public List<DegreePosition> startMarkPositions;
    public List<DegreePosition> finishMarkPositions;
    public Set<MarkDTO> marks;
    
    /**
     * <code>null</code> if the start waypoint does not have exactly two marks with valid positions; in this case,
     * {@link #startLineAdvantageousSide}, {@link #startLineAdvantageInMeters} and {@link #startLineLengthInMeters} are
     * also both <code>null</code>.
     */
    public Double startLineAngleToCombinedWind;
    public NauticalSide startLineAdvantageousSide;
    public Double startLineAdvantageInMeters;
    public Double startLineLengthInMeters;

    /**
     * <code>null</code> if the finish waypoint does not have exactly two marks with valid positions; in this case,
     * {@link #finishLineAdvantageousSide}, {@link #finishLineAdvantageInMeters} and
     * {@link #finishLineAdvantageInMeters} are also both <code>null</code>.
     */
    public Double finishLineAngleToCombinedWind;
    public NauticalSide finishLineAdvantageousSide;
    public Double finishLineAdvantageInMeters;
    public Double finishLineLengthInMeters;
}
