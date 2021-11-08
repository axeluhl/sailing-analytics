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
    private final String competitorColor;
    private final Date timePoint;
    private final Date timePointBefore;
    private final ManeuverType maneuverType;
    private final double speedBeforeInKnots;
    private final double speedAfterInKnots;
    private final double lowestSpeedInKnots;
    private final double maximumTurningRate;
    private final double averageTurningRate;
    private final Double maneuverLossInMeters;
    private final double directionChange;
    private final boolean markPassing;

    ManeuverTableData(final CompetitorDTO competitor, final String competitorColor, final ManeuverDTO maneuver) {
        this.competitorName = competitor.getName();
        this.competitorColor = competitorColor;
        this.timePoint = maneuver.getTimePoint();
        this.timePointBefore = maneuver.getTimePointBefore();
        this.maneuverType = maneuver.getType();
        this.speedBeforeInKnots = maneuver.getSpeedWithBearingBefore().speedInKnots;
        this.speedAfterInKnots = maneuver.getSpeedWithBearingAfter().speedInKnots;
        this.lowestSpeedInKnots = maneuver.getLowestSpeedInKnots();
        this.maneuverLossInMeters = maneuver.getManeuverLoss() == null ? 0.0 : maneuver.getManeuverLoss().getDistanceLost().getMeters();
        this.directionChange = maneuver.getDirectionChangeInDegrees();
        this.maximumTurningRate = maneuver.getMaxTurningRateInDegreesPerSecond();
        this.averageTurningRate = maneuver.getAvgTurningRateInDegreesPerSecond();
        this.markPassing = maneuver.getMarkPassingTimePoint() != null && maneuver.getMarkPassingSide() != null;
    }

    public String getCompetitorName() {
        return competitorName;
    }

    public String getCompetitorColor() {
        return competitorColor;
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

    public Double getManeuverLossInMeters() {
        return maneuverLossInMeters;
    }

    public double getDirectionChange() {
        return directionChange;
    }

}
