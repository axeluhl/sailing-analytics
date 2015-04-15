package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMarkMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimeRange;


public interface GPSFixStore {
    /**
     * Load all fixes that correspond to the {@link DeviceCompetitorMappingEvent}s found in the {@code raceLog}.
     */
    void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, AbstractLog<?, ?> log,
            Competitor competitor) throws TransformationException;

    /**
     * Load all fixes that correspond to the {@link DeviceMarkMappingEvent}s found in the {@code raceLog}.
     */
    void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, AbstractLog<?, ?> log, Mark mark) throws TransformationException,
    NoCorrespondingServiceRegisteredException;

    /**
     * Load all fixes that correspond to the {@code mapping}.
     */
    void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, DeviceMapping<Competitor> mapping)
            throws TransformationException, NoCorrespondingServiceRegisteredException;

    /**
     * Load all fixes that correspond to the {@code mapping}.
     */
    void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, DeviceMapping<Mark> mapping) throws TransformationException,
    NoCorrespondingServiceRegisteredException;

    void storeFix(DeviceIdentifier device, GPSFix fix) throws TransformationException, NoCorrespondingServiceRegisteredException;

    /**
     * Listeners are notified, whenever a {@link GPSFix} submitted by the {@code device}
     * is stored through the {@link #storeFix(DeviceIdentifier, GPSFix)} method.
     */
    void addListener(GPSFixReceivedListener listener, DeviceIdentifier device);

    /**
     * Remove the registrations of the listener for all devices.
     */
    void removeListener(GPSFixReceivedListener listener);
    
    TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device) throws TransformationException,
    NoCorrespondingServiceRegisteredException;
    
    long getNumberOfFixes(DeviceIdentifier device) throws TransformationException, NoCorrespondingServiceRegisteredException;
}
