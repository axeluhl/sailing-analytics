package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MarkpassingManeuverDTO extends ManeuverDTO {
    public Tack side;

    MarkpassingManeuverDTO() {}

    public MarkpassingManeuverDTO(ManeuverType type, Tack newTack, PositionDTO position, Date timepoint,
            SpeedWithBearingDTO speedWithBearingBefore, SpeedWithBearingDTO speedWithBearingAfter,
            double directionChangeInDegrees, Double maneuverLossInMeters, Tack side) {
        super(type, newTack, position, timepoint, speedWithBearingBefore, speedWithBearingAfter, directionChangeInDegrees,
                maneuverLossInMeters);
        this.side = side;
    }

    @Override
    public String toString(StringMessages stringMessages) {
        StringBuilder result = new StringBuilder(super.toString(stringMessages));
        result.append(", ");
        result.append(stringMessages.passedTo(side.name()));
        return result.toString();
    }
}
