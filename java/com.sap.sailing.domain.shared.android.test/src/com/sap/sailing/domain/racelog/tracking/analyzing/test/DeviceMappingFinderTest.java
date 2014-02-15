package com.sap.sailing.domain.racelog.tracking.analyzing.test;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceMapping;
import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.DeviceCompetitorMappingFinder;

public class DeviceMappingFinderTest extends AbstractRaceLogTrackingTest {
    private final Competitor competitor = new CompetitorImpl("comp", "Comp", null, null, null);
    private final Competitor competitor2 = new CompetitorImpl("comp2", "Comp2", null, null, null);
    private final Mark mark = new MarkImpl("mark");
    private final DeviceIdentifier device = new SmartphoneImeiIdentifier("imei");
    
    private List<DeviceMapping<Competitor>> getMappings() {
        return new DeviceCompetitorMappingFinder(log).analyze().get(competitor);
    }
    
    private void addMapping(RaceLogEventAuthor author, DeviceIdentifier device, long from, long to, Competitor item) {
        RaceLogEvent mapping = factory.createDeviceCompetitorMappingEvent(now, author, device, item, 0,
                new MillisecondsTimePoint(from), new MillisecondsTimePoint(to));
        log.add(mapping);
    }
    
    private void addMapping(RaceLogEventAuthor author, DeviceIdentifier device, long from, long to, Mark item) {
        RaceLogEvent mapping = factory.createDeviceMarkMappingEvent(now, author, device, item, 0,
                new MillisecondsTimePoint(from), new MillisecondsTimePoint(to));
        log.add(mapping);
    }
    
    @Test
    public void notDisturbedByMappingsForOtherItemsAndDevices() {
    	//two mappings for first competitor found? (competitor2 mappings should not "distract")
        addMapping(author, device, 10, 40, competitor);
        addMapping(author, device, 10, 40, competitor2);
        addMapping(author, device, 20, 30, competitor2);
        addMapping(author, device, 50, 90, competitor);
        Assert.assertEquals(2, getMappings().size());
        
        //are mappings merged? (mark mapping should not "distract")
        addMapping(author, device, 5, 100, competitor);
        addMapping(author, device, 15, 90, mark);
        Assert.assertEquals(1, getMappings().size());
        
        //overlap with same device -> should be merged
        addMapping(author, device, 30, 101, competitor);
        Assert.assertEquals(1, getMappings().size());
        
        //non-overlap with new device
        addMapping(author, new SmartphoneImeiIdentifier("imei2"), 110, 150, competitor);
        Assert.assertEquals(2, getMappings().size());
        
        //different device at start with overlap
        addMapping(author, new SmartphoneImeiIdentifier("imei3"), 0, 50, competitor);
        Assert.assertEquals(3, getMappings().size());
        
        //different device in middle -> split
        addMapping(author, new SmartphoneImeiIdentifier("imei3"), 60, 70, competitor);
        Assert.assertEquals(5, getMappings().size());
        
        //different device at end with overlap
        addMapping(author, new SmartphoneImeiIdentifier("imei4"), 140, 160, competitor);
        Assert.assertEquals(6, getMappings().size());
    }
    
    @Test
    public void openRangesCloseEachOther() {
    	//close one range
    	addMapping(author, device, 10, Long.MAX_VALUE, competitor);
    	addMapping(author, device, Long.MIN_VALUE, 20, competitor);
    	List<DeviceMapping<Competitor>> mappings = getMappings();
    	assertEquals(1, mappings.size());
    	DeviceMapping<Competitor> mapping = mappings.get(0);
    	assertEquals(10, mapping.getTimeRange().from().asMillis());
    	assertEquals(20, mapping.getTimeRange().to().asMillis());
    	
    	//another independent range
    	addMapping(author, device, 0, Long.MAX_VALUE, competitor);
    	addMapping(author, device, Long.MIN_VALUE, 5, competitor);
    	mappings = getMappings();
    	assertEquals(2, mappings.size());
    	mapping = mappings.get(0);
    	assertEquals(0, mapping.getTimeRange().from().asMillis());
    	assertEquals(5, mapping.getTimeRange().to().asMillis());
    	
    	//another closing mapping, that is closer, but is merged
    	addMapping(author, device, Long.MIN_VALUE, 3, competitor);
    	assertEquals(2, getMappings().size());
    	
    	//another opening mapping, so that two independent closed pairs are created
    	addMapping(author, device, 4, Long.MAX_VALUE, competitor);
    	assertEquals(3, getMappings().size());
    	
    	//open-ended at end, not closed
    	addMapping(author, device, 30, Long.MAX_VALUE, competitor);
    	assertEquals(4, getMappings().size());
    	
    	//open-ended at beginning, eats others up
    	addMapping(author, device, -10, Long.MAX_VALUE, competitor);
    	assertEquals(1, getMappings().size());
    }
}
