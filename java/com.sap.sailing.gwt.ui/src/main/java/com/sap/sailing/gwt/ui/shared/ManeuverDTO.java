package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ManeuverDTO implements IsSerializable {
    public ManeuverType type;

    public Tack newTack;

    public Position position;

    public Date timePoint;

    public Date timePointBefore;

    public SpeedWithBearingDTO speedWithBearingBefore;

    public SpeedWithBearingDTO speedWithBearingAfter;

    public double directionChangeInDegrees;

    public Double maneuverLossInMeters;

    public double maxTurningRateInDegreesPerSecond;

    public double avgTurningRateInDegreesPerSecond;

    public double lowestSpeedInKnots;

    public Date markPassingTimePoint;

    public NauticalSide markPassingSide;

    public ManeuverDTO() {}

    public ManeuverDTO(ManeuverType type, Tack newTack, Position position, Date timePoint, Date timePointBefore,
            SpeedWithBearingDTO speedWithBearingBefore, SpeedWithBearingDTO speedWithBearingAfter,
            double directionChangeInDegrees, Double maneuverLossInMeters, double maxTurningRateInDegreesPerSecond,
            double avgTurningRateInDegreesPerSecond, double lowestSpeedInKnots, Date markPassingTimePoint,
            NauticalSide markPassingSide) {
        this.type = type;
        this.newTack = newTack;
        this.position = position;
        this.timePoint = timePoint;
        this.timePointBefore = timePointBefore;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
        this.directionChangeInDegrees = directionChangeInDegrees;
        this.maneuverLossInMeters = maneuverLossInMeters;
        this.maxTurningRateInDegreesPerSecond = maxTurningRateInDegreesPerSecond;
        this.avgTurningRateInDegreesPerSecond = avgTurningRateInDegreesPerSecond;
        this.lowestSpeedInKnots = lowestSpeedInKnots;
        this.markPassingTimePoint = markPassingTimePoint;
        this.markPassingSide = markPassingSide;
    }

    public String toString(StringMessages stringMessages) {
        SpeedWithBearingDTO before = this.speedWithBearingBefore;
        SpeedWithBearingDTO after = this.speedWithBearingAfter;

        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_FULL);
        String timeAndManeuver = dateTimeFormat.format(this.timePoint) + ": " + this.type.name();
        String timePointBefore = " (started: " + dateTimeFormat.format(this.timePointBefore) + ")";
        String directionChange = stringMessages.directionChange() + ": "
                + ((int) Math.round(this.directionChangeInDegrees)) + " " + stringMessages.degreesShort() + " ("
                + ((int) Math.round(before.bearingInDegrees)) + " " + stringMessages.degreesShort() + " -> "
                + ((int) Math.round(after.bearingInDegrees)) + " " + stringMessages.degreesShort() + ")";
        String speedChange = stringMessages.speedChange() + ": "
                + NumberFormat.getDecimalFormat().format(after.speedInKnots - before.speedInKnots) + " "
                + stringMessages.knotsUnit() + " (" + NumberFormat.getDecimalFormat().format(before.speedInKnots) + " "
                + stringMessages.knotsUnit() + " -> " + NumberFormat.getDecimalFormat().format(after.speedInKnots) + " "
                + stringMessages.knotsUnit() + ")";
        String maxTurningRate = stringMessages.maxTurningRate() + ": "
                + NumberFormat.getDecimalFormat().format(this.maxTurningRateInDegreesPerSecond) + " "
                + stringMessages.degreesPerSecondUnit();
        String avgTurningRate = stringMessages.avgTurningRate() + ": "
                + NumberFormat.getDecimalFormat().format(this.avgTurningRateInDegreesPerSecond) + " "
                + stringMessages.degreesPerSecondUnit();
        String lowestSpeed = stringMessages.lowestSpeed() + ": "
                + NumberFormat.getDecimalFormat().format(this.lowestSpeedInKnots) + " " + stringMessages.knotsUnit();
        String maneuverLoss = this.maneuverLossInMeters == null ? ""
                : ("; " + stringMessages.maneuverLoss() + ": "
                        + NumberFormat.getDecimalFormat().format(this.maneuverLossInMeters) + " "
                        + stringMessages.metersUnit());
        String markPassing = markPassingTimePoint == null ? ""
                : "; " + stringMessages.markPassedToAt(
                        this.markPassingSide == null ? ""
                                : this.markPassingSide == NauticalSide.PORT ? stringMessages.portSide()
                                        : stringMessages.starboardSide(),
                        DateTimeFormat.getFormat(PredefinedFormat.TIME_FULL).format(this.markPassingTimePoint));
        String maneuverTitle = timeAndManeuver + timePointBefore + "; " + directionChange + "; " + speedChange + "; "
                + maxTurningRate + "; " + avgTurningRate + "; " + lowestSpeed + maneuverLoss + markPassing;
        return maneuverTitle;
    }
}
