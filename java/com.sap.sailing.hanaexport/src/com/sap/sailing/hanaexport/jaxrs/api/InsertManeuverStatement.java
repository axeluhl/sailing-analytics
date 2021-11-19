package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.hanaexport.jaxrs.api.InsertManeuverStatement.ManeuverTrackedRaceAndCompetitor;

public class InsertManeuverStatement extends AbstractPreparedInsertStatement<ManeuverTrackedRaceAndCompetitor> {
    static class ManeuverTrackedRaceAndCompetitor {
        private final Maneuver maneuver;
        private final TrackedRace trackedRace;
        private final Competitor competitor;
        public ManeuverTrackedRaceAndCompetitor(Maneuver maneuver, TrackedRace trackedRace, Competitor competitor) {
            super();
            this.maneuver = maneuver;
            this.trackedRace = trackedRace;
            this.competitor = competitor;
        }
        public Maneuver getManeuver() {
            return maneuver;
        }
        public TrackedRace getTrackedRace() {
            return trackedRace;
        }
        public Competitor getCompetitor() {
            return competitor;
        }
    }
    
    protected InsertManeuverStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"Maneuver\" (\"race\", \"regatta\", \"competitorId\", \"timepoint\", \"type\", \"newTack\", "+
                        "\"lossInMeters\", \"speedBeforeInKnots\", \"speedAfterInKnots\", "+
                        "\"courseBeforeInTrueDegrees\", \"courseAfterInTrueDegrees\", \"directionChangeInDegrees\", \"maximumTurningRateInDegreesPerSecond\", "+
                        "\"lowestSpeedInKnots\", \"toSide\") "+
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(ManeuverTrackedRaceAndCompetitor maneuverTrackedRaceAndCompetitor) throws SQLException {
        getPreparedStatement().setString(1, maneuverTrackedRaceAndCompetitor.getTrackedRace().getRace().getName());
        getPreparedStatement().setString(2, maneuverTrackedRaceAndCompetitor.getTrackedRace().getTrackedRegatta().getRegatta().getName());
        getPreparedStatement().setString(3, maneuverTrackedRaceAndCompetitor.getCompetitor().getId().toString());
        getPreparedStatement().setDate(4, new Date(maneuverTrackedRaceAndCompetitor.getManeuver().getTimePoint().asMillis()));
        getPreparedStatement().setString(5, maneuverTrackedRaceAndCompetitor.getManeuver().getType().name());
        getPreparedStatement().setString(6, maneuverTrackedRaceAndCompetitor.getManeuver().getNewTack().name());
        if (maneuverTrackedRaceAndCompetitor.getManeuver().getManeuverLoss() != null) {
            setDouble(7,
                    maneuverTrackedRaceAndCompetitor.getManeuver().getManeuverLoss().getDistanceSailedIfNotManeuveringProjectedOnMiddleManeuverAngle().getMeters()
                    -maneuverTrackedRaceAndCompetitor.getManeuver().getManeuverLoss().getDistanceSailedProjectedOnMiddleManeuverAngle().getMeters());
        } else {
            setDouble(7, 0);
        }
        setDouble(8, maneuverTrackedRaceAndCompetitor.getManeuver().getSpeedWithBearingBefore().getKnots());
        setDouble(9, maneuverTrackedRaceAndCompetitor.getManeuver().getSpeedWithBearingAfter().getKnots());
        setDouble(10, maneuverTrackedRaceAndCompetitor.getManeuver().getSpeedWithBearingBefore().getBearing().getDegrees());
        setDouble(11, maneuverTrackedRaceAndCompetitor.getManeuver().getSpeedWithBearingAfter().getBearing().getDegrees());
        setDouble(12, maneuverTrackedRaceAndCompetitor.getManeuver().getDirectionChangeInDegrees());
        setDouble(13, maneuverTrackedRaceAndCompetitor.getManeuver().getMaxTurningRateInDegreesPerSecond());
        setDouble(14, maneuverTrackedRaceAndCompetitor.getManeuver().getLowestSpeed().getKnots());
        getPreparedStatement().setString(15, maneuverTrackedRaceAndCompetitor.getManeuver().getToSide().name());
    }
}
