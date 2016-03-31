package com.sap.sailing.domain.racelogtracking.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMarkMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelog.tracking.GPSFixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.WithID;

public class RaceLogSensorDataTracker implements GPSFixReceivedListener<DoubleVectorFix> {
    private static final Logger logger = Logger.getLogger(RaceLogSensorDataTracker.class.getName());
    private final SensorFixStore sensorFixStore;
    private final AbstractRaceChangeListener raceChangeListener;
    private final ConcurrentHashMap<Competitor, List<DeviceMapping<Competitor>>> competitorMappings = new ConcurrentHashMap<>();
    private final Map<DeviceIdentifier, List<DeviceMapping<Competitor>>> competitorMappingsByDevices = new HashMap<>();
    private DynamicTrackedRace trackedRace;
    private final RegattaLog regattaLog;
    private final RegattaLogEventVisitor regattaLogEventVisitor;

    public RaceLogSensorDataTracker(DynamicTrackedRace trackedRace, DynamicTrackedRegatta regatta,
            SensorFixStore sensorFixStore) {

        this.trackedRace = trackedRace;
        this.sensorFixStore = sensorFixStore;
        this.regattaLog = regatta.getRegatta().getRegattaLog();

        regattaLogEventVisitor = new BaseRegattaLogEventVisitor() {
            @Override
            public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
                sensorFixStore.addListener(RaceLogSensorDataTracker.this, event.getDevice());
            }
        };
        regattaLog.addListener(regattaLogEventVisitor);

        raceChangeListener = new AbstractRaceChangeListener() {
            @Override
            public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
                if (newStatus.getStatus() == TrackedRaceStatusEnum.TRACKING) {
                    startTracking();
                } else {
                    stop();
                }
            }
        };
        trackedRace.addListener(raceChangeListener);
    }

    private void startTracking() {
        // update the device mappings (without loading the fixes, as the TrackedRace does this itself on startup)
        try {
            updateCompetitorMappings(false);
        } catch (DoesNotHaveRegattaLogException e) {
            logger.warning("Could not load update mark and competitor mappings as RegattaLog couldn't be found");
        }
        // add listeners for devices in mappings already present
        addListeners(competitorMappings);
        logger.info(String.format("Started tracking race-log race (%s)", regattaLog));
        // this wakes up all waiting race handles
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void stop() {

        regattaLog.removeListener(regattaLogEventVisitor);
        sensorFixStore.removeListener(this);
        trackedRace.removeListener(raceChangeListener);

    }

    private Map<Competitor, List<DeviceMapping<Competitor>>> getNewCompetitorMappings()
            throws DoesNotHaveRegattaLogException {
        return new RegattaLogDeviceCompetitorMappingFinder(regattaLog).analyze();
    }

    private Map<Mark, List<DeviceMapping<Mark>>> getNewMarkMappings() throws DoesNotHaveRegattaLogException {
        return new RegattaLogDeviceMarkMappingFinder(regattaLog).analyze();
    }

    private <ItemT extends WithID, FixT extends GPSFix> boolean hasMappingAlreadyBeenLoaded(
            DeviceMapping<ItemT> newMapping, List<DeviceMapping<ItemT>> oldMappings) {
        if (oldMappings == null) {
            return false;
        }
        for (DeviceMapping<ItemT> oldMapping : oldMappings) {
            if (newMapping.getDevice() == oldMapping.getDevice()
                    && newMapping.getTimeRange().liesWithin(oldMapping.getTimeRange())) {
                return true;
            }
        }
        return false;
    }

    private <ItemT extends WithID, FixT extends GPSFix> Map<DeviceIdentifier, List<DeviceMapping<ItemT>>> transformToMappingsByDevice(
            Map<ItemT, List<DeviceMapping<ItemT>>> mappings) {

        Map<DeviceIdentifier, List<DeviceMapping<ItemT>>> result = new HashMap<>();
        for (ItemT item : mappings.keySet()) {
            for (DeviceMapping<ItemT> mapping : mappings.get(item)) {
                List<DeviceMapping<ItemT>> list = result.get(mapping.getDevice());
                if (list == null) {
                    list = new ArrayList<>();
                    result.put(mapping.getDevice(), list);
                }
                list.add(mapping);
            }
        }

        return result;
    }

    /**
     * Adjusts the {@link #competitorMappings} map according to the competitor registrations for the race managed by
     * this tracked, either from the regatta log or the race log. Then, the {@link TrackedRace}'s start and end of
     * tracking time frame is {@link #updateStartAndEndOfTracking() updated} from the mapping intervals.
     * 
     * @param loadIfNotCovered
     *            if <code>true</code>, the GPS fixes for the mappings will be loaded based on a comparison of the
     *            previous mappings from {@link #competitorMappings} and the new mappings.
     * 
     * @throws DoesNotHaveRegattaLogException
     */
    private void updateCompetitorMappings(boolean loadIfNotCovered) throws DoesNotHaveRegattaLogException {
        // TODO remove fixes, if mappings have been removed

        // check if there are new time ranges not covered so far
        Map<Competitor, List<DeviceMapping<Competitor>>> newMappings = getNewCompetitorMappings();

        if (loadIfNotCovered) {
            for (Competitor competitor : newMappings.keySet()) {
                DynamicGPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                List<DeviceMapping<Competitor>> oldMappings = competitorMappings.get(competitor);

                for (DeviceMapping<Competitor> newMapping : newMappings.get(competitor)) {
                    if (!hasMappingAlreadyBeenLoaded(newMapping, oldMappings)) {
                        // try {
                        // // TODO: WTF
                        // // gpsFixStore.loadCompetitorTrack(track, newMapping);
                        // } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                        // logger.log(Level.WARNING, "Could not load competitor track " + newMapping.getMappedTo());
                        // }
                    }
                }
            }
        }
        competitorMappings.clear();
        competitorMappings.putAll(newMappings);
        competitorMappingsByDevices.clear();
        competitorMappingsByDevices.putAll(transformToMappingsByDevice(competitorMappings));
    }




    private <T extends WithID> void addListeners(Map<T, List<DeviceMapping<T>>> mappings) {
        for (List<DeviceMapping<T>> list : mappings.values()) {
            for (DeviceMapping<T> mapping : list) {
                sensorFixStore.addListener(this, mapping.getDevice());
            }
        }
    }

    @Override
    public void fixReceived(DeviceIdentifier device, DoubleVectorFix fix) {
        // TimePoint timePoint = fix.getTimePoint();
        // if (markMappingsByDevices.get(device) != null) {
        // for (DeviceMapping<Mark> mapping : markMappingsByDevices.get(device)) {
        // Mark mark = mapping.getMappedTo();
        // if (mapping.getTimeRange().includes(timePoint)) {
        // trackedRace.recordFix(mark, fix);
        // }
        // }
        // }
        //
        // if (competitorMappingsByDevices.get(device) != null) {
        // for (DeviceMapping<Competitor> mapping : competitorMappingsByDevices.get(device)) {
        // Competitor comp = mapping.getMappedTo();
        // if (mapping.getTimeRange().includes(timePoint)) {
        // if (fix instanceof GPSFixMoving) {
        // trackedRace.recordFix(comp, (GPSFixMoving) fix);
        // } else {
        // logger.log(
        // Level.WARNING,
        // String.format(
        // "Could not add fix for competitor (%s) in race (%s), as it is no GPSFixMoving, meaning it is missing COG/SOG values",
        // comp, params.getRaceLog()));
        // }
        // }
        // }
        // }
    }
}
