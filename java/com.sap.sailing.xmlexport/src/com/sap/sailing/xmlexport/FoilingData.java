package com.sap.sailing.xmlexport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class FoilingData {

    private final HttpServletRequest req;
    private final HttpServletResponse res;
    private final RacingEventService service;
    private final boolean useProvidedLeaderboard;
	
	public FoilingData(HttpServletRequest req, HttpServletResponse res, RacingEventService service) {
		this.req = req;
		this.res = res;
		this.service = service;
		this.useProvidedLeaderboard = false;
	}
	
    public String getAttribute(String name) {
        return this.req.getParameter(name);
    }

    public RacingEventService getService() {
        return service;
    }
    
    public Leaderboard getLeaderboard() throws IOException, ServletException {
    	Leaderboard leaderboard = null;
        if (!useProvidedLeaderboard) {
            final String leaderboardName = getAttribute("name");
            if (leaderboardName == null) {
                throw new ServletException("Use the name= parameter to specify the leaderboard");
            }
            leaderboard = getService().getLeaderboardByName(leaderboardName); 
            if (leaderboard == null) {
                throw new ServletException("Leaderboard " + leaderboardName + " not found.");
            }
        }
        return leaderboard;
    }
    
    public String perform() throws Exception {
    	StringBuffer result = new StringBuffer();
    	result.append("TimePoint").append(";");
    	result.append("RegattaName").append(";");
    	result.append("CourseIndex").append(";");
    	result.append("CompetitorName").append(";");
    	result.append("SpeedOverGroundInKnots").append(";");
    	result.append("RideHeight").append(";");
    	result.append("RideHeightPort").append(";");
    	result.append("RideHeightStarboard").append(";");
    	result.append("Heel").append(";");
    	result.append("Pitch").append(";");
    	result.append("AveragedWindSpeed").append(";");
    	result.append("WindBearingInDegrees").append("\n");
    	for (RaceColumn raceColumn : getLeaderboard().getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
				final List<Competitor> allCompetitors = trackedRace.getCompetitorsFromBestToWorst(/*timePoint*/trackedRace.getEndOfRace());
                for (Competitor competitor : allCompetitors) {
                	DynamicSensorFixTrack<Competitor, BravoFix> sensorTrack = trackedRace.getSensorTrack(competitor, BravoFixTrack.TRACK_NAME);
                	final Duration samplingInterval = Duration.ONE_SECOND;
                	final TimePoint fromTimePoint = trackedRace.getStartOfRace();
                	if (fromTimePoint != null) {
						final List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
						final WindSource windSource = new WindSourceImpl(WindSourceType.COMBINED);
						windSourcesToDeliver.add(windSource);
						final WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                		final TimePoint toTimePoint = trackedRace.getEndOfRace();
                		TimePoint timePointToConsider = fromTimePoint;
						int attemptedNumberOfFixes = 0;
                		while (toTimePoint == null ? (attemptedNumberOfFixes<5) : (timePointToConsider.compareTo(toTimePoint) < 0)) {
                			BravoFix bravoFixAtTimepoint = sensorTrack.getFirstFixAtOrAfter(timePointToConsider);
                			if (bravoFixAtTimepoint != null) {
                				Speed speedOfCompetitor = trackedRace.getTrack(competitor).getEstimatedSpeed(timePointToConsider);
                				double rideHeight = bravoFixAtTimepoint.get(BravoSensorDataMetadata.INSTANCE.RIDE_HEIGHT);
                				double rideHeightPort = bravoFixAtTimepoint.get(BravoSensorDataMetadata.INSTANCE.RIDE_HEIGHT_PORT_HULL);
                				double rideHeightStarboard = bravoFixAtTimepoint.get(BravoSensorDataMetadata.INSTANCE.RIDE_HEIGHT_STARBOARD_HULL);
                				double heel = bravoFixAtTimepoint.get(BravoSensorDataMetadata.INSTANCE.HEEL);
                				double pitch = bravoFixAtTimepoint.get(BravoSensorDataMetadata.INSTANCE.PITCH);
                				WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> windFix = windTrack.getAveragedWindWithConfidence(null, timePointToConsider);
                				SpeedWithConfidence<TimePoint> windFixSpeed = new SpeedWithConfidenceImpl<TimePoint>(new KnotSpeedImpl(windFix.getObject().getKnots()), windFix.getConfidence(), timePointToConsider);
                				Bearing windFixBearing = windFix.getObject().getBearing();
                				TrackedLegOfCompetitor leg = trackedRace.getCurrentLeg(competitor, timePointToConsider);

                				result.append(timePointToConsider.asDate().toString()).append(";");
                				result.append(trackedRace.getRaceIdentifier().getRegattaName()).append(";");
                				result.append(trackedRace.getRace().getCourse().getLegs().indexOf(leg.getLeg())).append(";");
                				result.append(competitor.getName()).append(";");
                				result.append(speedOfCompetitor.getKnots()).append(";");
                				result.append(rideHeight).append(";");
                				result.append(rideHeightPort).append(";");
                				result.append(rideHeightStarboard).append(";");
                				result.append(heel).append(";");
                				result.append(pitch).append(";");
                				result.append(windFixSpeed.getObject().getKnots()).append(";");
                				result.append(windFixBearing.getDegrees()).append("\n");
                			}
							timePointToConsider = timePointToConsider.plus(samplingInterval);
							attemptedNumberOfFixes++;
                		}
                	}
                }
            }
    	}
    	return result.toString();
    }
}
