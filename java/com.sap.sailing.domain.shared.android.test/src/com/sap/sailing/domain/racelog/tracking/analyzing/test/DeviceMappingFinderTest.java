package com.sap.sailing.domain.racelog.tracking.analyzing.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogDeviceCompetitorMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class DeviceMappingFinderTest extends AbstractRaceLogTrackingTest {
    private final Competitor competitor = new CompetitorImpl("comp", "Comp", null, null, null);
    private final Competitor competitor2 = new CompetitorImpl("comp2", "Comp2", null, null, null);
    private final DeviceIdentifier device = new SmartphoneImeiIdentifier("imei");
    
    private int time = 0;
    
    private List<DeviceMapping<Competitor>> getMappings() {
        return new RaceLogDeviceCompetitorMappingFinder(log).analyze().get(competitor);
    }
    
    private TimePoint t() {
        return new MillisecondsTimePoint(time++);
    }
    
    private TimePoint t(Long millis) {
        return millis == null ? null : new MillisecondsTimePoint(millis);
    }
    
    private Serializable addMapping(AbstractLogEventAuthor author, DeviceIdentifier device, Long from, Long to, Competitor item) {
        RaceLogEvent mapping = factory.createDeviceCompetitorMappingEvent(t(), author, t(), UUID.randomUUID(), device, item, 0,
                t(from), t(to));
        log.add(mapping);
        return mapping.getId();
    }
    
    private void closeMapping(AbstractLogEventAuthor author, DeviceIdentifier device, Serializable mappingId, long millis) {
        RaceLogEvent mapping = factory.createCloseOpenEndedDeviceMappingEvent(t(), author, t(), UUID.randomUUID(), 0, mappingId,
                new MillisecondsTimePoint(millis));
        log.add(mapping);
    }
    
    @Test
    public void notDisturbedByMappingsForOtherItemsAndDevices() {
    	//two mappings for first competitor found? (competitor2 mappings should not "distract")
        addMapping(author, device, 10L, 40L, competitor);
        addMapping(author, device, 10L, 40L, competitor2);
        addMapping(author, device, 20L, 30L, competitor2);
        addMapping(author, device, 50L, 90L, competitor);
        Assert.assertEquals(2, getMappings().size());
        
        //non-overlap with new device
        addMapping(author, new SmartphoneImeiIdentifier("imei2"), 110L, 150L, competitor);
        Assert.assertEquals(3, getMappings().size());
    }
    
    @Test
    public void openRangesAreClosed() {
    	//close one range
    	Serializable id = addMapping(author, device, 10L, null, competitor);
    	closeMapping(author, device, id, 20);
    	List<DeviceMapping<Competitor>> mappings = getMappings();
    	assertEquals(1, mappings.size());
    	DeviceMapping<Competitor> mapping = mappings.get(0);
    	assertEquals(10, mapping.getTimeRange().from().asMillis());
    	assertEquals(20, mapping.getTimeRange().to().asMillis());
    	
    	//another independent range
    	id = addMapping(author, device, 0L, null, competitor);
        closeMapping(author, device, id, 5);
    	mappings = getMappings();
    	assertEquals(2, mappings.size());
    	
    	//another closing mapping, that should take precedence (because added later)
        closeMapping(author, device, id, 3);
    	assertEquals(2, getMappings().size());
    	assertEquals(3, getMappings().get(1).getTimeRange().to().asMillis());
    	
    	//open-ended at end, not closed
    	addMapping(author, device, 30L, null, competitor);
    	assertEquals(3, getMappings().size());
    }
}
