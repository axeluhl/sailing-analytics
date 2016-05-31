package com.sap.sailing.domain.racelog.tracking;

import java.util.function.Consumer;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.WithID;


public interface GPSFixStore {
    /**
     * Load all fixes that correspond to the {@link RegattaLogDeviceCompetitorMappingEvent}s found in the {@code raceLog}.
     */
    void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, RegattaLog log,
            Competitor competitor) throws TransformationException;

    /**
     * Load all fixes that correspond to the {@link RegattaLogDeviceMarkMappingEvent}s found in the {@code raceLog}.
     */
    void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RegattaLog log, Mark mark) throws TransformationException,
    NoCorrespondingServiceRegisteredException;

    /**
     * Load all fixes within the start and end time point (inclusive) that correspond to the
     * {@link RegattaLogDeviceMarkMappingEvent}s found in the {@code raceLog}.
     * 
     * @param start
     *            if <code>null</code>, the start of the time range for which to load fixes is only constrained by the
     *            device mapping intervals
     * @param end
     *            if <code>null</code>, the end of the time range for which to load fixes is only constrained by the
     *            device mapping intervals
     */
    void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RegattaLog log, Mark mark, TimePoint start,
            TimePoint end) throws TransformationException, NoCorrespondingServiceRegisteredException;

    /**
     * Load all fixes that correspond to the {@code mapping}.
     */
    void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, DeviceMapping<Competitor> mapping,
            TimePoint start, TimePoint end)
            throws TransformationException, NoCorrespondingServiceRegisteredException;

    /**
     * Load all fixes that correspond to the {@code mapping}.
     */
    void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, DeviceMapping<Mark> mapping) throws TransformationException,
    NoCorrespondingServiceRegisteredException;
    
    /**
     * Load all fixes for both marks and competitors according to the {@code mapping}.
     */
    void loadTrack(DynamicGPSFixTrack<WithID, ?> track, DeviceMapping<WithID> mapping)
            throws NoCorrespondingServiceRegisteredException, TransformationException;

    void storeFix(DeviceIdentifier device, GPSFix fix) throws TransformationException, NoCorrespondingServiceRegisteredException;

    /**
     * Listeners are notified, whenever a {@link GPSFix} submitted by the {@code device}
     * is stored through the {@link #storeFix(DeviceIdentifier, GPSFix)} method.
     */
    void addListener(FixReceivedListener<GPSFix> listener, DeviceIdentifier device);

    /**
     * Remove the registrations of the listener for all devices.
     */
    void removeListener(FixReceivedListener<GPSFix> listener);
    
    TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device) throws TransformationException,
    NoCorrespondingServiceRegisteredException;
    
    long getNumberOfFixes(DeviceIdentifier device) throws TransformationException, NoCorrespondingServiceRegisteredException;
    
    <FixT extends GPSFix> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier deviceIdentifier, TimePoint start, TimePoint end, boolean inclusive) throws NoCorrespondingServiceRegisteredException,
    TransformationException;
}
