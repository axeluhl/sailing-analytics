package com.sap.sailing.domain.racelogtracking.test.impl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.fixtracker.RegattaLogDeviceMappings;
import com.sap.sailing.domain.racelogtracking.test.AbstractGPSFixStoreTest;
import com.sap.sse.common.MultiTimeRange;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class DeviceMappingsAndSensorFixStoreLockingTest extends AbstractGPSFixStoreTest {
    @Rule
    public Timeout GPSFixStoreListenerTestTimeout = new Timeout(3 * 1000);
    
    @Test
    public void deviceMappingsAndSensorFixStoreShouldNotCauseADeadlock() {
        final Competitor comp = DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", null, null, null,
                null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
        final AbstractLogEventAuthor author = new LogEventAuthorImpl("author", 0);
        final DeviceIdentifier device = new SmartphoneImeiIdentifier("a");
        
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final RegattaLog regattaLog = new RegattaLogImpl("regattalog");
        RegattaLogDeviceMappings<WithID> mappings = new RegattaLogDeviceMappings<WithID>(Collections.singleton(regattaLog), "Test") {

            @Override
            protected void deviceIdAdded(DeviceIdentifier deviceIdentifier) {
                try {
                    barrier.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                store.addListener((device, fix) -> {
                    return null;
                }, deviceIdentifier);
            }

            @Override
            protected void deviceIdRemoved(DeviceIdentifier deviceIdentifier) {
            }

            @Override
            protected void newTimeRangesCovered(WithID item,
                    Map<RegattaLogDeviceMappingEvent<WithID>, MultiTimeRange> newlyCoveredTimeRanges) {
            }
        };
        store.addListener((dev,  fix)-> {
            try {
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            mappings.forEachMappingOfDeviceIncludingTimePoint(device, new MillisecondsTimePoint(1), (mapping) ->{});
            return null;
        }, device);
        
        new Thread() {
            public void run() {
                regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                        device, new MillisecondsTimePoint(0), new MillisecondsTimePoint(2)));
                
            };
        }.start();
        
        store.storeFix(device, new DoubleVectorFixImpl(new MillisecondsTimePoint(1), new Double[]{0.0}));
    }

}
