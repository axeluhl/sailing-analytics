package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMarkMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelog.tracking.FixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.TimeRangeImpl;

public class RaceLogGPSFixTracker extends AbstractRaceLogFixTracker {
    private static final Logger logger = Logger.getLogger(RaceLogGPSFixTracker.class.getName());
    
    private final GPSFixStore gpsFixStore;
    private final RegattaLogEventVisitor regattaLogEventVisitor = new BaseRegattaLogEventVisitor() {

        @Override
        public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
            logger.log(Level.FINE, "New mapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            try {
                competitorMappings.updateMappings(true);
            } catch (DoesNotHaveRegattaLogException e) {
                logger.warning("Could not load update competitor mappings as RegattaLog couldn't be found");
            }
            gpsFixStore.addListener(fixReceivedListener, event.getDevice());
        }

        @Override
        public void visit(RegattaLogDeviceMarkMappingEvent event) {
            logger.log(Level.FINE, "New mapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            try {
                markMappings.updateMappings(true);
            } catch (DoesNotHaveRegattaLogException e) {
                logger.warning("Could not load update mark mappings as RegattaLog couldn't be found");
            }
            gpsFixStore.addListener(fixReceivedListener, event.getDevice());
        }
        
        @Override
        public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
            logger.log(Level.FINE, "Mapping closed: " + event.getDeviceMappingEventId());
            updateMappingsAndAddListeners();
        }
        
        @Override
        public void visit(RegattaLogRevokeEvent event) {
            logger.log(Level.FINE, "Mapping revoked for: " + event.getRevokedEventId());
            updateMappingsAndAddListeners();
        };
    };
    
    private final FixReceivedListener<GPSFix> fixReceivedListener = new GPSFixReceivedListener();

    private final CompetitorRaceLogMappings competitorMappings = new CompetitorRaceLogMappings();
    private final RaceLogMappingWrapper<Mark> markMappings = new MarkRaceLogMappings();
    
    public RaceLogGPSFixTracker(DynamicTrackedRegatta regatta, DynamicTrackedRace trackedRace, GPSFixStore gpsFixStore) {
        super(regatta, trackedRace);
        this.gpsFixStore = gpsFixStore;
        startTracking();
    }

    @Override
    protected void loadFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo) {
        competitorMappings.forEachMapping((mapping) -> loadCompetitorFixes(
                new TimeRangeImpl(loadFixesFrom, loadFixesTo).intersection(mapping.getTimeRange()), mapping));
        markMappings.forEachMapping((mapping) -> loadMarkFixes(
                new TimeRangeImpl(loadFixesFrom, loadFixesTo).intersection(mapping.getTimeRange()), mapping));
    }

    private void loadCompetitorFixes(TimeRange timeRangeToLoad, DeviceMapping<Competitor> mapping) {
        DynamicGPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(mapping.getMappedTo());
        try {
            gpsFixStore.loadFixes(track::addGPSFix, mapping.getDevice(), timeRangeToLoad.from(), timeRangeToLoad.to(), true);
        } catch (NoCorrespondingServiceRegisteredException | TransformationException e) {
            logger.log(Level.WARNING, "Could not load track for competitor: " + mapping.getMappedTo() + "; device: " + mapping.getDevice());
        }
    }
    
    private void loadMarkFixes(TimeRange timeRangeToLoad, DeviceMapping<Mark> mapping) {
        DynamicGPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mapping.getMappedTo());
        try {
            gpsFixStore.loadFixes(track::addGPSFix, mapping.getDevice(), timeRangeToLoad.from(), timeRangeToLoad.to(), true);
        } catch (NoCorrespondingServiceRegisteredException | TransformationException e) {
            logger.log(Level.WARNING, "Could not load track for competitor: " + mapping.getMappedTo() + "; device: " + mapping.getDevice());
        }
    }

    @Override
    protected RegattaLogEventVisitor getRegattaLogEventVisitor() {
        return regattaLogEventVisitor;
    }

    @Override
    protected void updateMappingsAndAddListenersImpl() {
        try {
            competitorMappings.updateMappings(true);
        } catch (Exception e) {
            logger.warning("Could not load update competitor mappings as RegattaLog couldn't be found");;
        }
        // add listeners for devices in mappings already present
        competitorMappings.forEachDevice((device) -> gpsFixStore.addListener(fixReceivedListener, device));
        
        try {
            markMappings.updateMappings(true);
        } catch (Exception e) {
            logger.warning("Could not load update mark mappings as RegattaLog couldn't be found");;
        }
        // add listeners for devices in mappings already present
        markMappings.forEachDevice((device) -> gpsFixStore.addListener(fixReceivedListener, device));
    }
    
    @Override
    protected void stopTracking() {
        super.stopTracking();
        gpsFixStore.removeListener(fixReceivedListener);
    }
    
    private final class CompetitorRaceLogMappings extends RaceLogMappingWrapper<Competitor> {
        @Override
        protected Map<Competitor, List<DeviceMapping<Competitor>>> calculateMappings() throws DoesNotHaveRegattaLogException {
            Map<Competitor, List<DeviceMapping<Competitor>>> result = new HashMap<>();
            forEachRegattaLog((log) -> result.putAll(new RegattaLogDeviceCompetitorMappingFinder(log).analyze()));
            return result;
        }
        
        @Override
        protected void mappingAdded(DeviceMapping<Competitor> mapping) {
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(mapping.getMappedTo());
            try {
                gpsFixStore.loadCompetitorTrack(track, mapping);
            } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                logger.log(Level.WARNING, "Could not load competitor track " + mapping.getMappedTo());
            }
        }

        @Override
        protected void mappingChanged(DeviceMapping<Competitor> oldMapping, DeviceMapping<Competitor> newMapping) {
            // TODO can the new time range be bigger than the old one? 
            // In this case we would need to load the additional time range.
        }
        
        @Override
        protected void mappingRemoved(DeviceMapping<Competitor> mapping) {
            // TODO if tracks are always associated to only one device mapping, we could remove tracks here
            // TODO remove listener from store if there is no mapping left for the DeviceIdentifier
        }
    }
    
    private final class MarkRaceLogMappings extends RaceLogMappingWrapper<Mark> {
        @Override
        protected Map<Mark, List<DeviceMapping<Mark>>> calculateMappings() throws DoesNotHaveRegattaLogException {
            Map<Mark, List<DeviceMapping<Mark>>> result = new HashMap<>();
            forEachRegattaLog((log) -> result.putAll(new RegattaLogDeviceMarkMappingFinder(log).analyze()));
            return result;
        }
        
        @Override
        protected void mappingAdded(DeviceMapping<Mark> mapping) {
            DynamicGPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mapping.getMappedTo());
            try {
                gpsFixStore.loadMarkTrack(track, mapping);
            } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                logger.log(Level.WARNING, "Could not load mark track " + mapping.getMappedTo());
            }
        }
        
        @Override
        protected void mappingChanged(DeviceMapping<Mark> oldMapping, DeviceMapping<Mark> newMapping) {
            // TODO can the new time range be bigger than the old one? 
            // In this case we would need to load the additional time range.
        }
        
        @Override
        protected void mappingRemoved(DeviceMapping<Mark> mapping) {
            // TODO if tracks are always associated to only one device mapping, we could remove tracks here
            // TODO remove listener from store if there is no mapping left for the DeviceIdentifier
        }
    }
    
    private final class GPSFixReceivedListener implements FixReceivedListener<GPSFix> {
        @Override
        public final void fixReceived(DeviceIdentifier device, GPSFix fix) {
            final TimePoint timePoint = fix.getTimePoint();
            markMappings.forEachMappingOfDeviceIncludingTimePoint(device, timePoint, (mapping) -> {
                Mark mark = mapping.getMappedTo();
                final DynamicGPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(mark);
                final GPSFix firstFixAtOrAfter;
                final boolean forceFix;
                markTrack.lockForRead();
                try {
                    forceFix = Util.isEmpty(markTrack.getRawFixes()) ||
                            (firstFixAtOrAfter=markTrack.getFirstFixAtOrAfter(timePoint)) != null &&
                                firstFixAtOrAfter.getTimePoint().equals(timePoint);
                } finally {
                    markTrack.unlockAfterRead();
                }
                if (forceFix) {
                    trackedRace.recordFix(mark, fix, /* only when in tracking interval */ false); // force fix into track
                } else {
                    trackedRace.recordFix(mark, fix);
                }
            });
            
            competitorMappings.forEachMappingOfDeviceIncludingTimePoint(device, timePoint, (mapping) -> {
                Competitor comp = mapping.getMappedTo();
                if (fix instanceof GPSFixMoving) {
                    trackedRace.recordFix(comp, (GPSFixMoving) fix);
                } else {
                    logger.log(Level.WARNING, String.format("Could not add fix for competitor (%s) in race (%s), as it"
                            + " is no GPSFixMoving, meaning it is missing COG/SOG values", comp, trackedRace.getRace().getName()));
                }
            });
        }
    }
}
