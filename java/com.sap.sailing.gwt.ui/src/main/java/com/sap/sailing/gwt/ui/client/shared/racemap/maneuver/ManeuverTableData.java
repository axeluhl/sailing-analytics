package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.Date;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.SpeedDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sse.common.Duration;

/**
 * Data object used in {@link ManeuverTablePanel} only holding competitor and maneuver information.
 */
class ManeuverTableData {

    private final CompetitorDTO competitor;
    private final Date timePoint;
    private final Date timePointBefore;
    private final ManeuverType maneuverType;
    private final Duration duration;
    private final SpeedWithBearingDTO speedIn;
    private final SpeedWithBearingDTO speedOut;
    private final SpeedDTO minSpeed;
    private final double turnRate;
    private final Double loss;
    private final double directionChange;

    ManeuverTableData(final CompetitorDTO competitor, final ManeuverDTO maneuver) {
        this.competitor = competitor;
        this.timePoint = maneuver.timePoint;
        this.timePointBefore = maneuver.timePointBefore;
        this.maneuverType = maneuver.type;
        this.duration = maneuver.duration;
        this.speedIn = maneuver.speedWithBearingBefore;
        this.speedOut = maneuver.speedWithBearingAfter;
        this.minSpeed = maneuver.minSpeed;
        this.loss = maneuver.maneuverLossInMeters;
        this.directionChange = maneuver.directionChangeInDegrees;
        this.turnRate = duration == null ? 0 : Math.abs(directionChange) / duration.asSeconds();
    }

    public CompetitorDTO getCompetitor() {
        return competitor;
    }

    public Date getTimePoint() {
        return timePoint;
    }

    public Date getTimePointBefore() {
        return timePointBefore;
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

    public double getDirectionChange() {
        return directionChange;
    }

}
