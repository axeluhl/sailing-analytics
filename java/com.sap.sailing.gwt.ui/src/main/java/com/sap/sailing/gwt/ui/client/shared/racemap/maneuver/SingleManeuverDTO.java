package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.Date;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sse.common.Duration;

public class SingleManeuverDTO {
    private final CompetitorDTO competitor;
    private final Date time;
    private final ManeuverType maneuverType;
    private final Duration duration;
    private final SpeedWithBearingDTO speedIn;
    private final SpeedWithBearingDTO speedOut;
    private final SpeedWithBearingDTO minSpeed;
    private final double turnRate;
    private final Double loss;
    private final double directionChangeInDegrees;

    public SingleManeuverDTO(CompetitorDTO competitor, Date time, ManeuverType maneuverType, Duration duration,
            SpeedWithBearingDTO speedIn, SpeedWithBearingDTO speedOut, SpeedWithBearingDTO minSpeed, double turnRate,
            Double loss, double directionChangeInDegrees) {
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
        this.directionChangeInDegrees = directionChangeInDegrees;
    }

    public CompetitorDTO getCompetitor() {
        return competitor;
    }

    public Date getTime() {
        return time;
    }

    public ManeuverType getManeuverType() {
        return maneuverType;
    }

    public Double getDurationAsSeconds() {
        return duration == null ? null : duration.asSeconds();
    }

    public Double getSpeedInAsKnots() {
        return speedIn == null ? null : speedIn.speedInKnots;
    }

    public Double getSpeedOutAsKnots() {
        return speedOut == null ? null : speedOut.speedInKnots;
    }

    public Double getMinSpeedAsKnots() {
        return minSpeed == null ? null : minSpeed.speedInKnots;
    }

    public double getTurnRate() {
        return turnRate;
    }

    public Double getLoss() {
        return loss;
    }

    public double getDirectionChangeInDegrees() {
        return directionChangeInDegrees;
    }

}
