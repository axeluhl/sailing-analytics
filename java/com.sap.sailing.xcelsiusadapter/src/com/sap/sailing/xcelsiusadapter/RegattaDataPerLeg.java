package com.sap.sailing.xcelsiusadapter;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.domain.tracking.impl.MarkPassingByTimeComparator;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;

public class RegattaDataPerLeg extends Action {
	public RegattaDataPerLeg(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
		super(req, res, service, maxRows);
	}
	

	public void perform() throws Exception {	
        		
        final Regatta regatta = getRegatta(); // Get regatta data from request (get value for regatta name from URL parameter regatta)
        // if the regatta does not exist a tag <message> will be returned with a text message from function getRegatta(). 
        if(regatta == null){ 
        	return; 
        }        
        
        final Document doc = new Document(); // initialize xml document
		final Element regatta_node = addNamedElement(doc,"regatta"); //add root to xml
    	addNamedElementWithValue(regatta_node, "name", regatta.getName());
    	addNamedElementWithValue(regatta_node, "boat_class", regatta.getBoatClass().getName());
        
        /*
         * Races
         * */
        final HashMap<String, RaceDefinition> races = getRaces(regatta); // get races for the regatta
        final Element races_node = addNamedElement(regatta_node,"races"); //add node that contains all races
        
        
        for (final RaceDefinition race : races.values()) { // for each race in the list        	        
        	final Element race_node = addNamedElement(races_node,"race"); //add race node for the current race
        	addNamedElementWithValue(race_node, "name", race.getName()); // add name node to current race
        	
        	//skip race if not tracked
        	final TrackedRace trackedRace = getTrackedRace(regatta, race);
	        if (trackedRace == null) {
	        	continue; 
	        } 
	        
            final TimePoint raceStarted = getTimePoint(trackedRace); // get TimePoint for when the race started    

            long minNextLegStart = raceStarted.asMillis(); //variable for keeping track of when the first competitor started the next leg
            TimePoint legStarted = new MillisecondsTimePoint(minNextLegStart); // get TimePoint for when the leg started
           
            addNamedElementWithValue(race_node, "start_time_ms", raceStarted.asMillis()); // add the starttime to the race
            if (trackedRace.getEndOfRace() != null) {
            	addNamedElementWithValue(race_node, "assumed_end_ms", trackedRace.getEndOfRace().asMillis()); // add the assumed enddtime
            } else {
            	addNamedElementWithValue(race_node, "assumed_end_ms", 0); // add the assumed enddtime
            }
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(raceStarted.asDate());
            addNamedElementWithValue(race_node, "start_time_year", cal.get(Calendar.YEAR));
            int month = cal.get(Calendar.MONTH) + 1;
            addNamedElementWithValue(race_node, "start_time_month", month);
            addNamedElementWithValue(race_node, "start_time_day", cal.get(Calendar.DAY_OF_MONTH));
            addNamedElementWithValue(race_node, "start_time_hour", cal.get(Calendar.HOUR_OF_DAY));
            addNamedElementWithValue(race_node, "start_time_minute", cal.get(Calendar.MINUTE));
            addNamedElementWithValue(race_node, "start_time_second", cal.get(Calendar.SECOND));
            addNamedElementWithValue(race_node, "start_time_formatted", (cal.get(Calendar.DAY_OF_MONTH) < 10 ? ("0" + cal.get(Calendar.DAY_OF_MONTH)) : cal.get(Calendar.DAY_OF_MONTH)) + "." +
            															(month < 10 ? ("0" + month) : month) + "." +
            															cal.get(Calendar.YEAR) + " - " +
            															(cal.get(Calendar.HOUR_OF_DAY) < 10 ? ("0" + cal.get(Calendar.HOUR_OF_DAY)) : cal.get(Calendar.HOUR_OF_DAY)) + ":" +
            															(cal.get(Calendar.MINUTE) < 10 ? ("0" + cal.get(Calendar.MINUTE)) : cal.get(Calendar.MINUTE)) + ":" +
            															(cal.get(Calendar.SECOND) < 10 ? ("0" + cal.get(Calendar.SECOND)) : cal.get(Calendar.SECOND)));
            
            Pair<Double, Double> averageWindSpeedofRace = calculateAverageWindSpeedofRace(trackedRace);
            String wind_strength = "";
            if (averageWindSpeedofRace != null) {
	            if (averageWindSpeedofRace.getA() < 4) {
	            	wind_strength = "Very light";
	            } else if (averageWindSpeedofRace.getA() >= 4 && averageWindSpeedofRace.getA() < 8) {
	            	wind_strength = "Light";
	            } else if (averageWindSpeedofRace.getA() >= 8 && averageWindSpeedofRace.getA() < 14) {
	            	wind_strength = "Medium";
	            } else if (averageWindSpeedofRace.getA() >= 14 && averageWindSpeedofRace.getA() < 20) {
	            	wind_strength = "Strong";
	            } else if (averageWindSpeedofRace.getA() >= 20) {
	            	wind_strength = "Very strong";
	            }
            }
            
            Double wind_speed = 0.0;
            Double wind_confi = 0.0;
            
            if (averageWindSpeedofRace != null) {
            	wind_speed = averageWindSpeedofRace.getA();
            	wind_confi = averageWindSpeedofRace.getB();
            }
            
            addNamedElementWithValue(race_node, "average_wind_speed", wind_speed);
            addNamedElementWithValue(race_node, "average_wind_speed_confidence", wind_confi);
            addNamedElementWithValue(race_node, "wind_strength", wind_strength);
            

            /*
             * Legs
             * */
            int i = 0; // initialize leg index            
            TrackedLeg previousLeg = null;
            
            LinkedHashMap<String, Long> legTimesAlternate = new LinkedHashMap<String, Long>(); // Map for  leg times calculated by mark passings
           
            final Element legs_node = addNamedElement(race_node,"legs"); // add element that holds all legs for the race
            
            for (final TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
                final Leg leg = trackedLeg.getLeg();    	                
                final Element leg_node = addNamedElement(legs_node,"leg"); // add element for single leg
                
                addNamedElementWithValue(leg_node, "leg_number", ++i);
                addNamedElementWithValue(leg_node, "mark_from", leg.getFrom().getName());
                addNamedElementWithValue(leg_node, "mark_to", leg.getTo().getName());
                addNamedElementWithValue(leg_node, "leg_type", (trackedLeg.getLegType(legStarted).toString()));
                                
                /*
                 * Competitors
                 * */
                LinkedHashMap<Competitor, Integer> ranks = trackedLeg.getRanks(legStarted); // get the ranking information for the race (at leg start time)                																							
                final Element competitor_data_node = addNamedElement(leg_node,"competitor_data"); // add element that holds the leg summary data for each competitor
                
                for (final Competitor competitor : ranks.keySet()) { // Get competitor data
                	
                    TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor); // Get data
                    
                    TimePoint compareLegEnd = new MillisecondsTimePoint(0);   
                    
                    // time elapsed / when did the competitor pass the end mark of the leg
                    for(MarkPassing mp : trackedRace.getMarkPassings(competitor)){
                    	if (mp.getWaypoint() == leg.getTo()){
                    		compareLegEnd = mp.getTimePoint();
                    		break;
                    	}
                    }
                    
                    // leg time / based on elapsed time 
                    if(legTimesAlternate.containsKey(competitor.getName())){    
	                    legTimesAlternate.put(competitor.getName(), compareLegEnd.asMillis() - legTimesAlternate.get(competitor.getName()) - raceStarted.asMillis());                		
                		
                	}else{                		
                		legTimesAlternate.put(competitor.getName(), compareLegEnd.asMillis() - raceStarted.asMillis());
                	}
                    
                	
                	final TimePoint compLegTimeAlt = new MillisecondsTimePoint(legTimesAlternate.get(competitor.getName()));
                    
                    final TimePoint compFinishedLeg = new MillisecondsTimePoint(compareLegEnd.asMillis());
                    
                    //plausibility check 
                    //competitor has finished the leg and the leg end time is not the race start time
                	if(trackedLegOfCompetitor.hasFinishedLeg(compFinishedLeg) && compareLegEnd.asMillis() != 0){
                		// Calculate rank loss/gain
                		int posGL = 0;
	                    if (previousLeg != null) {
	                        posGL = trackedLegOfCompetitor.getRank(compFinishedLeg) - previousLeg.getTrackedLeg(competitor).getRank(compFinishedLeg);
	                    }
	                    
	                                        
	                    final Element competitor_node = addNamedElement(competitor_data_node,"competitor");
	                    try{
	                    	addNamedElementWithValue(competitor_node, "name", competitor.getName());
		                    addNamedElementWithValue(competitor_node, "nationality", competitor.getTeam().getNationality().getThreeLetterIOCAcronym());
		                    addNamedElementWithValue(competitor_node, "sail_id", competitor.getBoat().getSailID());
		                    String sail_id = competitor.getBoat().getSailID();
		                    if (sail_id.matches("^[A-Z]{3}\\s[0-9]*")) {		                    	
		                    	
		                    	Pattern regex = Pattern.compile("(^[A-Z]{3})\\s([0-9]*)");
		                    	Matcher regexMatcher = regex.matcher(sail_id);
		                    	try {
		                    	    String resultString = regexMatcher.replaceAll("$1$2");
		                    	    addNamedElementWithValue(competitor_node, "sail_id_formatted", resultString);
		                    	} catch (Exception e) {
		                    	    e.printStackTrace();
		                    	}

		                    	
		                    } else if (sail_id.matches("^[A-Z]{3}\\S[0-9]*")) {
		                    	addNamedElementWithValue(competitor_node, "sail_id_formatted", sail_id);
		                    } else if (sail_id.matches("[0-9]*")){
		                    	addNamedElementWithValue(competitor_node, "sail_id_formatted", competitor.getTeam().getNationality().getThreeLetterIOCAcronym() + sail_id);
		                    } else {
		                    	addNamedElementWithValue(competitor_node, "sail_id_formatted", sail_id);
		                    }
		                    addNamedElementWithValue(competitor_node, "leg_finished_time_ms", compFinishedLeg.asMillis());
		                    addNamedElementWithValue(competitor_node, "time_elapsed_ms", compFinishedLeg.asMillis() - raceStarted.asMillis());
		                    addNamedElementWithValue(competitor_node, "leg_time_ms", compLegTimeAlt.asMillis()); 
		                    addNamedElementWithValue(competitor_node, "leg_rank", trackedLegOfCompetitor.getRank(compFinishedLeg));	
		                    addNamedElementWithValue(competitor_node, "race_final_rank", trackedRace.getRank(competitor));
		                    addNamedElementWithValue(competitor_node, "rank_gain", posGL*-1); //Ranks Gained/Lost		                    
		                    addNamedElementWithValue(competitor_node, "gap_to_leader_s", trackedLegOfCompetitor.getGapToLeaderInSeconds(compFinishedLeg));
		                    addNamedElementWithValue(competitor_node, "ww_distance_to_leader_m", trackedLegOfCompetitor.getWindwardDistanceToOverallLeader(compFinishedLeg).getMeters());
//		                    addNamedElementWithValue(competitor_node, "avg_xte", trackedLegOfCompetitor.getAverageCrossTrackError(compFinishedLeg).getMeters());
//		                    addNamedElementWithValue(competitor_node, "avg_vmg_kn", trackedLegOfCompetitor.getAverageVelocityMadeGood(compFinishedLeg).getKnots());//windwardSpeed over ground on leg finished time
		                    addNamedElementWithValue(competitor_node, "avg_speed_og_kn", trackedLegOfCompetitor.getAverageSpeedOverGround(compFinishedLeg).getKnots());
		                    addNamedElementWithValue(competitor_node, "avg_speed_ww_kn", trackedLegOfCompetitor.getAverageVelocityMadeGood(compFinishedLeg).getKnots());//windwardSpeed over ground on leg finished time
		                    addNamedElementWithValue(competitor_node, "distance_traveled_m", trackedLegOfCompetitor.getDistanceTraveled(compFinishedLeg).getMeters());
		                    addNamedElementWithValue(competitor_node, "number_of_jibes",  trackedLegOfCompetitor.getNumberOfJibes(compFinishedLeg));
		                    addNamedElementWithValue(competitor_node, "number_of_tacks",  trackedLegOfCompetitor.getNumberOfTacks(compFinishedLeg));
		                    addNamedElementWithValue(competitor_node, "number_of_penalty_circles",  trackedLegOfCompetitor.getNumberOfPenaltyCircles(compFinishedLeg));
		                    addNamedElementWithValue(competitor_node, "average_wind_speed", wind_speed);
		                    addNamedElementWithValue(competitor_node, "average_wind_speed_confidence", wind_confi);
		                    
//		                    Waypoint finish = trackedRace.getRace().getCourse().getLastWaypoint();
//		                    
//		                    Iterable<MarkPassing> markpassings = trackedRace.getMarkPassingsInOrder(finish);
//		                    Iterator<MarkPassing> iter = markpassings.iterator();
//		                    MarkPassing firstMarkPassing = null;
//		                    if (iter.hasNext()) {
//		                    	firstMarkPassing = iter.next();
//		                    }
//		                    
//		                    
////		                    Map<Waypoint, NavigableSet<MarkPassing>> markPassingsForWaypoint = new HashMap<Waypoint, NavigableSet<MarkPassing>>();
////		                    for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
////		                        markPassingsForWaypoint.put(waypoint, new ConcurrentSkipListSet<MarkPassing>(
////		                                MarkPassingByTimeComparator.INSTANCE));
////		                    }
////		                    
////		                    NavigableSet<MarkPassing> markPassingsInOrder = markPassingsForWaypoint.get(finish);
////		                    MarkPassing firstMarkPassing = null;
////		                    synchronized (markPassingsInOrder) {
////		                        if (!markPassingsInOrder.isEmpty()) {
////		                            firstMarkPassing = markPassingsInOrder.first();
////		                        }
////		                    }
//		                    TimePoint timeOfFirstMarkPassing = null;
//		                    if (firstMarkPassing != null) {
//		                        timeOfFirstMarkPassing = firstMarkPassing.getTimePoint();
//		                    }
//		                    
//		                    trackedRace.getWindwardDistanceToOverallLeader(competitor, timeOfFirstMarkPassing);
		                    
		                    
		                    
		                    

		                    // assign the smallest start time for the next leg
		                    minNextLegStart = (minNextLegStart > compFinishedLeg.asMillis() ? compFinishedLeg.asMillis() : minNextLegStart);   
	                    }catch(Exception ex){	                    	
	                    	//competitor_data_node.removeContent(competitor_node); // if the competitor dataset is not complete, remove it from the list
	                    }
	                    
	                
                    
                	}else{
                		System.err.print("Competitpr Skipped:" + competitor.getName() + "; Race:" + race.getName() +"; Leg+"+ ((Integer)i).toString());
                	}
                	
                	
                } // competitor data end
                legStarted = new MillisecondsTimePoint(minNextLegStart);
                minNextLegStart = Long.MAX_VALUE;
                previousLeg = trackedLeg;
                
                // if the leg does not contain any competitor info
                if (competitor_data_node.getChildren("competitor").size() == 0){ 
                	legs_node.removeContent(leg_node);
                }
                	
            } //leg end	  
            
            // if the race does not contain any leg info
            if (legs_node.getChildren("leg").size() == 0){ 
            	races_node.removeContent(race_node);            	
            }          
	        
        } // regatta end            
        sendDocument(doc, regatta.getName() + ".xml");// output doc to client
            
           
        
	} // function end
	
	private Pair<Double, Double> calculateAverageWindSpeedofRace(TrackedRace trackedRace) {
		Pair<Double, Double> result = null;
		if (trackedRace.getEndOfRace() != null) {
	        TimePoint fromTimePoint = trackedRace.getStartOfRace();
	        TimePoint toTimePoint = trackedRace.getEndOfRace(); 
	        long resolutionInMilliseconds = 60 * 1000 * 5; // 5 min
	
	        List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
	        WindSourceImpl windSource = new WindSourceImpl(WindSourceType.COMBINED);
	        windSourcesToDeliver.add(windSource);
	
	        double sumWindSpeed = 0.0; 
	        double sumWindSpeedConfidence = 0.0; 
	        int speedCounter = 0;
	        
	        int numberOfFixes = (int) ((toTimePoint.asMillis() - fromTimePoint.asMillis())/resolutionInMilliseconds);
	        TimePoint newestEvent = trackedRace.getTimePointOfNewestEvent();
	
	        WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
	        TimePoint timePoint = fromTimePoint;
	        for (int i = 0; i < numberOfFixes && toTimePoint != null && timePoint.compareTo(toTimePoint) < 0; i++) {
	            WindWithConfidence<Pair<Position, TimePoint>> averagedWindWithConfidence = windTrack.getAveragedWindWithConfidence(null, timePoint);
	            if (averagedWindWithConfidence != null) {
	            	double windSpeedinKnots = averagedWindWithConfidence.getObject().getKnots();
	                double confidence = averagedWindWithConfidence.getConfidence();
	
	                sumWindSpeed += windSpeedinKnots;
	            	sumWindSpeedConfidence += confidence;
	            	
	            	speedCounter++;
	            }
	            timePoint = new MillisecondsTimePoint(timePoint.asMillis() + resolutionInMilliseconds);
	        }
	
	        if(speedCounter > 0) {
	            double averageWindSpeed = sumWindSpeed / speedCounter;
	        	double averageWindSpeedConfidence = sumWindSpeedConfidence / speedCounter;
	        	
	        	result = new Pair<Double, Double>(averageWindSpeed, averageWindSpeedConfidence);
	        } 	
		} else {
			result = new Pair<Double, Double>(0.0, 0.0);
		}
        return result;
	}

	private void addNamedElementWithValue(Element parent, String newChildName, Integer i) {
		if(i == null){
			addNamedElementWithValue(parent,newChildName, "0");
		}else{
			addNamedElementWithValue(parent,newChildName, i.toString());
		}
		
	}

	private void addNamedElementWithValue(Element parent, String newChildName, Double dbl) {
		if(dbl == null){
			addNamedElementWithValue(parent,newChildName, "0");
		}else{
			addNamedElementWithValue(parent,newChildName, dbl.toString());
		}
		
	}
	
	private void addNamedElementWithValue(Element parent, String newChildName, Long l) {
		if(l == null){
			addNamedElementWithValue(parent,newChildName, "0");
		}else{
			addNamedElementWithValue(parent,newChildName, l.toString());
		}
		
	}

	private Element addNamedElement(Document doc, String newChildName) {
		final Element newChild = new Element(newChildName);
		doc.addContent(newChild);
		return newChild;
	}

	private Element addNamedElementWithValue(Element parent, String newChildName, String value){
		final Element newChild = new Element(newChildName);
        newChild.addContent(value);
        parent.addContent(newChild);
        return newChild;
	}
	
	private Element addNamedElement(Element parent, String newChildName){
		 final Element newChild = new Element(newChildName);
         parent.addContent(newChild);
         return newChild;
	}
	
	
}
