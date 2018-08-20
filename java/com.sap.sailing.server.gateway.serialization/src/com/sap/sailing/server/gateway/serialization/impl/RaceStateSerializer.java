package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Calendar;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastFlagsFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.ReadonlyGateStartRacingProcedure;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceStateOfSameDayHelper;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class RaceStateSerializer implements JsonSerializer<Pair<RaceColumn, Fleet>> {
    private final RaceLogResolver raceLogResolver;

    public RaceStateSerializer(RaceLogResolver raceLogResolver) {
        this.raceLogResolver = raceLogResolver;
    }

    @Override
    public JSONObject serialize(Pair<RaceColumn, Fleet> raceColumnAndFleet) {
        RaceColumn raceColumn = raceColumnAndFleet.getA();
        Fleet fleet = raceColumnAndFleet.getB();
        
        JSONObject result = new JSONObject();
        result.put("raceName", raceColumn.getName());
        result.put("fleetName", fleet.getName());
        result.put("trackedRaceLinked", Boolean.valueOf(raceColumn.getTrackedRace(fleet) != null));
        RaceIdentifier raceIdentifier = raceColumn.getRaceIdentifier(fleet);
        result.put("trackedRaceName", raceIdentifier != null ? raceIdentifier.getRaceName() : null);
        result.put("trackedRaceId", raceIdentifier != null ? raceIdentifier.toString() : null);
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog != null && !raceLog.isEmpty()) {
            ReadonlyRaceState state = ReadonlyRaceStateImpl.getOrCreate(raceLogResolver, raceLog);
            RaceLogRaceStatus status = state.getStatus();
            TimePoint startTime = state.getStartTime();
            TimePoint finishedTime = state.getFinishedTime();
            JSONObject raceLogStateJson = new JSONObject();
            result.put("raceState", raceLogStateJson);
            raceLogStateJson.put("startTime", startTime != null ? startTime.toString() : null);
            raceLogStateJson.put("endTime", finishedTime != null ? finishedTime.toString() : null);
            raceLogStateJson.put("lastStatus", status.name());
            ReadonlyGateStartRacingProcedure procedure = state.getTypedReadonlyRacingProcedure(ReadonlyGateStartRacingProcedure.class);
            if (procedure != null) {
                raceLogStateJson.put("pathfinderId", procedure.getPathfinder());
                raceLogStateJson.put("gateLineOpeningTime", procedure.getGateLaunchStopTime());
            }
            AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
            RaceLogFlagEvent abortingFlagEvent = abortingFlagFinder.analyze();
            LastFlagsFinder lastFlagFinder = new LastFlagsFinder(raceLog);
            RaceLogFlagEvent lastFlagEvent = LastFlagsFinder.getMostRecent(lastFlagFinder.analyze());
            if (lastFlagEvent != null) {
                setLastFlagField(raceLogStateJson, lastFlagEvent.getUpperFlag().name(), lastFlagEvent.getLowerFlag().name(), lastFlagEvent.isDisplayed());
            } else if (status.equals(RaceLogRaceStatus.UNSCHEDULED) && abortingFlagEvent != null) {
                setLastFlagField(raceLogStateJson, abortingFlagEvent.getUpperFlag().name(), abortingFlagEvent.getLowerFlag().name(), abortingFlagEvent.isDisplayed());
            } else {
                setLastFlagField(raceLogStateJson, null, null, null);
            }
        }
        return result;
    }
    
    private void setLastFlagField(JSONObject raceLogStateJson, String upperFlagName, String lowerFlagName, Boolean isDisplayed) {
        raceLogStateJson.put("lastUpperFlag", upperFlagName);
        raceLogStateJson.put("lastLowerFlag", lowerFlagName);
        raceLogStateJson.put("displayed", isDisplayed);
    }
    
    public boolean isRaceStateOfSameDay(Pair<RaceColumn, Fleet> raceColumnAndFleet, Calendar dayToCheck, Duration clientTimeZoneOffset) {
        RaceColumn raceColumn = raceColumnAndFleet.getA();
        Fleet fleet = raceColumnAndFleet.getB();
        boolean result = false;
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog != null && !raceLog.isEmpty()) {
            TimePoint startTime = new StartTimeFinder(raceLogResolver, raceLog).analyze().getStartTime();
            TimePoint finishedTime = new FinishedTimeFinder(raceLog).analyze();
            RaceLogFlagEvent abortingFlagEvent = new AbortingFlagFinder(raceLog).analyze();
            TimePoint abortingTime = abortingFlagEvent != null ? abortingFlagEvent.getLogicalTimePoint() : null;
            result = RaceStateOfSameDayHelper.isRaceStateOfSameDay(startTime, finishedTime, abortingTime, dayToCheck, clientTimeZoneOffset);
        }
        return result;
    }
}
