package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Duration;

public class MarkpassingManeuverDTO extends ManeuverDTO {
    public NauticalSide side;

    MarkpassingManeuverDTO() {}

    public MarkpassingManeuverDTO(ManeuverType type, Tack newTack, Position position, Date timepoint,
            SpeedWithBearingDTO speedWithBearingBefore, SpeedWithBearingDTO speedWithBearingAfter,
            double directionChangeInDegrees, Double maneuverLossInMeters, NauticalSide side, Duration duration, SpeedWithBearing minSpeed) {
        super(type, newTack, position, timepoint, speedWithBearingBefore, speedWithBearingAfter, directionChangeInDegrees,
                maneuverLossInMeters, duration, minSpeed);
        this.side = side;
    }

    @Override
    public String toString(StringMessages stringMessages) {
        StringBuilder result = new StringBuilder(super.toString(stringMessages));
        result.append(", ");
        result.append(stringMessages.passedTo(side==null ? "" : side == NauticalSide.PORT ? stringMessages.portSide() : stringMessages.starboardSide()));
        return result.toString();
    }
}
