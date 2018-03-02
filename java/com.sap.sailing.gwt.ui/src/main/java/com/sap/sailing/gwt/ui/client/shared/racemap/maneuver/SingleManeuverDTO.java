package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.Date;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sse.common.Duration;

public class SingleManeuverDTO {
    public final CompetitorDTO competitor;
    public final Date time;
    public final ManeuverType maneuverType;
    public final Duration duration;
    public final SpeedWithBearingDTO speedIn;
    public final SpeedWithBearingDTO speedOut;
    public final SpeedWithBearingDTO minSpeed;
    public final double turnRate;
    public final double loss;
    public final Bearing direction;

    public SingleManeuverDTO(CompetitorDTO competitor, Date time, ManeuverType maneuverType, Duration duration,
            SpeedWithBearingDTO speedIn, SpeedWithBearingDTO speedOut, SpeedWithBearingDTO minSpeed, double turnRate, double loss, Bearing direction) {
        super();
        this.competitor = competitor;
        this.time = time;
        this.maneuverType = maneuverType;
        this.duration = duration;
        this.speedIn = speedIn;
        this.speedOut = speedOut;
        this.minSpeed = minSpeed;
        this.turnRate = turnRate;
        this.loss = loss;
        this.direction = direction;
    }
}
