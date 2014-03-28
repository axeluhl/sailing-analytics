package com.sap.sailing.domain.racelog.tracking.analyzing.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceMapping;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.DeviceCompetitorMappingFinder;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;

public class DeviceMappingFinderTest extends AbstractRaceLogTrackingTest {
    private final Competitor competitor = new CompetitorImpl("comp", "Comp", null, null, null);
    private final Competitor competitor2 = new CompetitorImpl("comp2", "Comp2", null, null, null);
    private final Mark mark = new MarkImpl("mark");
    private final DeviceIdentifier device = new SmartphoneImeiIdentifier("imei");
    
    private int time = 0;
    
    private List<DeviceMapping<Competitor>> getMappings() {
        return new DeviceCompetitorMappingFinder(log).analyze().get(competitor);
    }
    
    private TimePoint t() {
        return new MillisecondsTimePoint(time++);
    }
    
    private TimePoint t(Long millis) {
        return millis == null ? null : new MillisecondsTimePoint(millis);
    }
    
    private Serializable addMapping(RaceLogEventAuthor author, DeviceIdentifier device, Long from, Long to, Competitor item) {
        RaceLogEvent mapping = factory.createDeviceCompetitorMappingEvent(t(), author, t(), UUID.randomUUID(), device, item, 0,
                t(from), t(to));
        log.add(mapping);
        return mapping.getId();
    }
    
    private Serializable addMapping(RaceLogEventAuthor author, DeviceIdentifier device, Long from, Long to, Mark item) {
        RaceLogEvent mapping = factory.createDeviceMarkMappingEvent(t(), author, t(), UUID.randomUUID(), device, item, 0,
                t(from), t(to));
        log.add(mapping);
        return mapping.getId();
    }
    
    private void closeMapping(RaceLogEventAuthor author, DeviceIdentifier device, Serializable mappingId, long millis) {
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
        
        //are mappings merged? (mark mapping should not "distract")
        addMapping(author, device, 5L, 100L, competitor);
        addMapping(author, device, 15L, 90L, mark);
        Assert.assertEquals(1, getMappings().size());
        
        //overlap with same device -> should be merged
        addMapping(author, device, 30L, 101L, competitor);
        Assert.assertEquals(1, getMappings().size());
        
        //non-overlap with new device
        addMapping(author, new SmartphoneImeiIdentifier("imei2"), 110L, 150L, competitor);
        Assert.assertEquals(2, getMappings().size());
        
        //different device at start with overlap
        addMapping(author, new SmartphoneImeiIdentifier("imei3"), 0L, 50L, competitor);
        Assert.assertEquals(3, getMappings().size());
        
        //different device in middle -> split
        addMapping(author, new SmartphoneImeiIdentifier("imei3"), 60L, 70L, competitor);
        Assert.assertEquals(5, getMappings().size());
        
        //different device at end with overlap
        addMapping(author, new SmartphoneImeiIdentifier("imei4"), 140L, 160L, competitor);
        Assert.assertEquals(6, getMappings().size());
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
    	
    	//open-ended at beginning, eats others up
    	addMapping(author, device, -10L, null, competitor);
    	assertEquals(1, getMappings().size());
    }
    
    @Test
    public void doesLaterMappingThatIncludesOtherOverwrite() throws ParseException {
        DeviceIdentifier cde = new SmartphoneImeiIdentifier("cde");
        DeviceIdentifier abc = new SmartphoneImeiIdentifier("abc");
        DateFormat df = new SimpleDateFormat("mm:ss");
        long m1600 = df.parse("16:00").getTime();
        long m1550 = df.parse("15:50").getTime();
        long m1615 = df.parse("16:15").getTime();
        long m1610 = df.parse("16:10").getTime();
        addMapping(author, abc, m1600, m1610, competitor);
        addMapping(author, cde, m1550, m1615, competitor);
        
        assertEquals(1, getMappings().size());
    }
    
    @Test
    public void doesLaterMappingThatLiesWithinBreakOtherIntoPieces() throws ParseException {
        DeviceIdentifier cde = new SmartphoneImeiIdentifier("cde");
        DeviceIdentifier abc = new SmartphoneImeiIdentifier("abc");
        DateFormat df = new SimpleDateFormat("mm:ss");
        long m1600 = df.parse("16:00").getTime();
        long m1550 = df.parse("15:50").getTime();
        long m1615 = df.parse("16:15").getTime();
        long m1610 = df.parse("16:10").getTime();
        addMapping(author, cde, m1550, m1615, competitor);
        addMapping(author, abc, m1600, m1610, competitor);
        
        assertEquals(3, getMappings().size());
    }
}
