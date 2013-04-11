package com.sap.sailing.gwt.ui.shared;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CoursePositionsDTO implements IsSerializable {
    public List<PositionDTO> waypointPositions;
    public List<PositionDTO> startMarkPositions;
    public List<PositionDTO> finishMarkPositions;
    public Set<MarkDTO> marks;
}
