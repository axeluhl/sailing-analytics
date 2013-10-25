package com.sap.sailing.gwt.ui.shared;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.dto.PositionDTO;

public class CoursePositionsDTO implements IsSerializable {
    public List<PositionDTO> waypointPositions;
    public List<PositionDTO> startMarkPositions;
    public List<PositionDTO> finishMarkPositions;
    public Set<MarkDTO> marks;
    public Double startLineAngleToCombinedWind;
    public NauticalSide startLineAdvantageousSide;
    public Double startLineAdvantageInMeters;
    public Double finishLineAngleToCombinedWind;
    public NauticalSide finishLineAdvantageousSide;
    public Double finishLineAdvantageInMeters;
}
