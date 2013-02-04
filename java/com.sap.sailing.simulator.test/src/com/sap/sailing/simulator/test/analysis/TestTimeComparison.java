package com.sap.sailing.simulator.test.analysis;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.impl.Util.Quadruple;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.ui.server.SimulatorServiceImpl;
import com.sap.sailing.gwt.ui.shared.ConfigurationException;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.RequestTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.ResponseTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.simulator.test.util.TracTracReader;

public class TestTimeComparison {

    private List<PositionDTO> turnPoints = null;
    private List<SimulatorWindDTO> allPoints = null;

    private Map<Integer, String> boatClassesIndexes = null;
    private Map<Integer, String> averageWindFlags = null;
    private List<Integer> timeStepMillisecondsSizes = null;

	@Test
	public void runSimulation() throws ConfigurationException, ClassNotFoundException, IOException {
		
		File dir = new File("C:\\Users\\i059829\\workspace\\sapsailingcapture\\java\\com.sap.sailing.simulator.test");
		String[] flist = dir.list(new FilenameFilter() { 
	         public boolean accept(File dir, String filename)
             { return filename.endsWith(".data"); }
	         } );
		
		TracTracReader ttreader = new TracTracReader(flist);
		
		List<TrackedRace> lst = ttreader.read();
		
		List<String> csvRows = new ArrayList<String>();
		
		for( TrackedRace tr : lst ) {
			
			String csvLine = "";
			RegattaAndRaceIdentifier id = tr.getRaceIdentifier();
			
			//System.out.println(id.getRaceName() + "/" + id.getRegattaName());
			
			Iterable<Competitor> competitors = tr.getRace().getCompetitors();
			List<Leg> legs = tr.getRace().getCourse().getLegs();
			
			for( Competitor competitor: competitors) {
					
				GPSFixTrack<Competitor, GPSFixMoving> track = tr.getTrack(competitor);
				track.lockForRead();
				Iterator<GPSFixMoving> it = track.getFixes().iterator();
				for ( Leg leg : legs ) {
					List<SimulatorWindDTO> allPoints = new ArrayList<SimulatorWindDTO>();
					MarkPassing mp = tr.getMarkPassing(competitor, leg.getTo());
					GPSFixMoving current;
					while( mp.getTimePoint().after((current = it.next()).getTimePoint()) ) {
						System.out.println(leg + ":" + current);
					}
					//track.lockForRead();
					//track.get
					//System.out.println(mp);
				}
					
				
			}
			
			/*for( TrackedLeg tl : trackedLegs ) {
				
				for( Competitor c : competitors ) {
					
					TrackedLegOfCompetitor tlc = tl.getTrackedLeg(c);
				
					
				}
			}*/
			
			
			
			
		
			/*
			   Iterator<RaceDefinition> racIter = races.iterator();
        while (racIter.hasNext()) {
            RaceDefinition race = racIter.next();
            System.out.println("Race: \"" + race.getName() + "\", \"" + regatta + "\"");

            RaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta, race.getName());
            TrackedRace tr = service.getExistingTrackedRace(raceIdentifier);

            System.out.println("Competitors:");
            Iterable<Competitor> competitors = tr.getRace().getCompetitors();
            System.out.println("" + competitors);
            Iterator<Competitor> comIter = competitors.iterator();
            while (comIter.hasNext()) {
                Competitor com = comIter.next();
                GPSFixTrack<Competitor, GPSFixMoving> track = null;
                Iterable<GPSFixMoving> fixes = null;
                track = tr.getTrack(com);

                track.lockForRead();
                fixes = track.getFixes();
                GPSFixMoving fix = fixes.iterator().next();
                System.out.println("" + com.getName() + ", First GPS-Fix: " + fix.getPosition().getLatDeg() + ", " + fix.getPosition().getLngDeg());
                track.unlockAfterRead();

            }*/
			 
			
			
		}
		
	}
	
    private void getTotalTime(final int boatClassIndex, final int useRealAverageWindSpeed, final int stepDurationMilliseconds) throws ConfigurationException {

        final SimulatorServiceImpl simulatorService = new SimulatorServiceImpl();
        final RequestTotalTimeDTO requestData = new RequestTotalTimeDTO(boatClassIndex, this.allPoints, this.turnPoints, useRealAverageWindSpeed == 1,
                stepDurationMilliseconds, true);
        final ResponseTotalTimeDTO receiveData = simulatorService.getTotalTime_new(requestData);

        final SpeedWithBearing averageWind = simulatorService.getAverageWind();
        final double stepSizeMeters = simulatorService.getStepSizeMeters();
        int stepIndex = 0;
        
        String event = "unknown";
        String race = "unknown";
        String competitor = "unknown";
        int leg = 1;
        double gps_time = 0;
        double pd1_time = 0;
        double pd2_time = 0;
        double pd3_time = 0;
        double wind_avg_speed = 0;
        double boat_avg_speed = 0;
        double boat_avg_bearing = 0;

        System.err.println("==================================================");
        System.err.println(this.boatClassesIndexes.get(boatClassIndex));
        System.err.println("average wind speed = " + averageWind.getKnots() + " knots, bearing = " + averageWind.getBearing().getDegrees() + " degrees");
        System.err.println("step size = " + stepSizeMeters + " meters");
        stepIndex = 0;
        for (final Quadruple<PositionDTO, PositionDTO, Double, Double> segment : receiveData.segments) {
            System.err.println("segment " + stepIndex + " from [" + segment.getA().latDeg + "," + segment.getA().lngDeg + "] to ["
                    + segment.getB().latDeg + "," + segment.getB().lngDeg + "], length = " + segment.getC() + " meters, time = " + segment.getD() / 1000.
                    + " seconds");
            stepIndex++;
        }
        System.err.println(this.boatClassesIndexes.get(boatClassIndex) + ", " + this.averageWindFlags.get(useRealAverageWindSpeed) + ", "
                + stepDurationMilliseconds + "milliseconds timestep, total time: " + receiveData.totalTimeSeconds
                + " seconds");
        System.err.println("==================================================");
    }

}
