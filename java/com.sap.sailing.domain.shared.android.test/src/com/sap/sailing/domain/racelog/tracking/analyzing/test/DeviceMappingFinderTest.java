package com.sap.sailing.domain.racelog.tracking.analyzing.test;

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

public class MappingAnalyzerTest extends AbstractRaceLogTrackingTest {
    private final Competitor competitor = new CompetitorImpl("comp", "Comp", null, null, null);
    private final Competitor competitor2 = new CompetitorImpl("comp2", "Comp2", null, null, null);
    private final Mark mark = new MarkImpl("mark");
    private final DeviceIdentifier device = new SmartphoneImeiIdentifier("imei");
    
    private List<DeviceMapping<Competitor>> getMappings() {
        return new DeviceCompetitorMappingFinder(log).analyze().get(competitor);
    }
    
    private void addMapping(RaceLogEventAuthor author, long from, long to, Competitor item) {
        RaceLogEvent mapping = factory.createDeviceCompetitorMappingEvent(now, author, device, item, 0,
                new MillisecondsTimePoint(from), new MillisecondsTimePoint(to));
        log.add(mapping);
    }
    
    private void addMapping(RaceLogEventAuthor author, long from, long to, Mark item) {
        RaceLogEvent mapping = factory.createDeviceMarkMappingEvent(now, author, device, item, 0,
                new MillisecondsTimePoint(from), new MillisecondsTimePoint(to));
        log.add(mapping);
    }
    
    @Test
    public void isCorrectMappingGenerate() {
        addMapping(author, 10, 40, competitor);
        addMapping(author, 10, 40, competitor2);
        addMapping(author, 20, 30, competitor2);
        addMapping(author, 50, 90, competitor);
        Assert.assertEquals(2, getMappings().size());
        
        addMapping(author, 5, 100, competitor);
        addMapping(author, 15, 90, mark);
        Assert.assertEquals(1, getMappings().size());
        
        addMapping(author, 110, 150, competitor);
        Assert.assertEquals(2, getMappings().size());
        
        addMapping(author, 0, 50, competitor);
        Assert.assertEquals(3, getMappings().size());
        
        addMapping(author, 60, 70, competitor);
        Assert.assertEquals(5, getMappings().size());
        
        addMapping(author, 140, 160, competitor);
        Assert.assertEquals(6, getMappings().size());
    }
}
