package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.Date;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;

/**
 * Data object used in {@link ManeuverTablePanel} only holding competitor and maneuver information.
 */
class ManeuverTableData {

    private final String competitorName;
    private final Date timePoint;
    private final Date timePointBefore;
    private final ManeuverType maneuverType;
    private final double speedBeforeInKnots;
    private final double speedAfterInKnots;
    private final double lowestSpeedInKnots;
    private final double maximumTurningRate;
    private final double averageTurningRate;
    private final Double maneuverLoss;
    private final double directionChange;
    private final boolean markPassing;

    ManeuverTableData(final CompetitorDTO competitor, final ManeuverDTO maneuver) {
        this.competitorName = competitor.getName();
        this.timePoint = maneuver.timePoint;
        this.timePointBefore = maneuver.timePointBefore;
        this.maneuverType = maneuver.type;
        this.speedBeforeInKnots = maneuver.speedWithBearingBefore.speedInKnots;
        this.speedAfterInKnots = maneuver.speedWithBearingAfter.speedInKnots;
        this.lowestSpeedInKnots = maneuver.lowestSpeedInKnots;
        this.maneuverLoss = maneuver.maneuverLossInMeters;
        this.directionChange = maneuver.directionChangeInDegrees;
        this.maximumTurningRate = maneuver.maxTurningRateInDegreesPerSecond;
        this.averageTurningRate = maneuver.avgTurningRateInDegreesPerSecond;
        this.markPassing = maneuver.markPassingTimePoint != null && maneuver.markPassingSide != null;
    }

    public String getCompetitorName() {
        return competitorName;
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

    public boolean isMarkPassing() {
        return markPassing;
    }

    public double getSpeedBeforeInKnots() {
        return speedBeforeInKnots;
    }

    public double getSpeedAfterInKnots() {
        return speedAfterInKnots;
    }

    public double getSpeedChangeInKnots() {
        return getSpeedAfterInKnots() - getSpeedBeforeInKnots();
    }

    public double getLowestSpeedInKnots() {
        return lowestSpeedInKnots;
    }

    public double getMaximumTurningRate() {
        return maximumTurningRate;
    }

    public double getAverageTurningRate() {
        return averageTurningRate;
    }

    public Double getManeuverLoss() {
        return maneuverLoss;
    }

    public double getDirectionChange() {
        return directionChange;
    }

}
