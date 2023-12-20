package com.sap.sailing.datamining.impl.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTackTypeSegmentContext;
import com.sap.sailing.datamining.impl.data.TrackedRaceWithContext;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Color;
import com.sap.sse.common.Distance;

public class TestTackTypeSegments {
    TrackedRace trackedRace;
    GPSFixTrack<Competitor, GPSFixMoving> track;
    Competitor competitor;
    
    @Before
    public void setup () {
        Map<Competitor, Boat> competitors = new HashMap<>();
        Competitor competitor = new CompetitorImpl(UUID.randomUUID(), "testPerson", "HP", Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
                new PersonImpl("testPerson", new NationalityImpl("GER"),
                /* dateOfBirth */null, "This is famous " + "testPerson")), new PersonImpl("Rigo van Maas",
                new NationalityImpl("NED"),
                /* dateOfBirth */null, "This is Rigo, the coach")), 
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
        DynamicBoat boat = new BoatImpl(competitor.getId(), "testPerson" + "'s boat", null, null, null);
        competitors.put(competitor, boat);
        
    }
        


    
    @Test
    public void testingMissingMarkPassing () {
        
        final HasRaceOfCompetitorContext element = Mockito.mock(HasRaceOfCompetitorContext.class);
        final TrackedRaceWithContext trackedRaceContext = Mockito.mock(TrackedRaceWithContext.class);
        final TackTypeSegmentRetrievalProcessor resultTTSegmentsRetrieval = Mockito.mock(TackTypeSegmentRetrievalProcessor.class);
        
        
        when(element.getTrackedRaceContext()).thenReturn(trackedRaceContext);  
        when(element.getCompetitor()).thenReturn(competitor);
        when(trackedRaceContext.getTrackedRace().getTrack(element.getCompetitor())).thenReturn(track);  
        
        
        //vorhandene segmente in liste und distanzen zusammenrechnen
        Iterable<HasTackTypeSegmentContext> allTTSegments = resultTTSegmentsRetrieval.retrieveData(element);
        Distance sum=null;
        for (HasTackTypeSegmentContext oneTTSegment : allTTSegments) {
            sum = sum.add(oneTTSegment.getDistance());
        }
//        Distance sum2 = null;
//        result.forEach(currentcase -> sum2.add(currentcase.getDistance()));
//        
        //ergebnis der distanzen checken
        assertTrue(sum==null);
        assertEquals(null, sum);
    }
    //missing + skipped
    
   
    @Test
    public void testingFixExactOnMarkPassing () {       
    }
   
    @Test
    public void testingOpenEndedRace () {}
   
    @Test
    public void testingFinishedRace () {}
    
    
}
