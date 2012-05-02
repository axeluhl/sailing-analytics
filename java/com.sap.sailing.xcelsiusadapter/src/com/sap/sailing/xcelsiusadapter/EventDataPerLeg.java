package com.sap.sailing.xcelsiusadapter;



import java.util.HashMap;
import java.util.LinkedHashMap;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.jdom.Document;
import org.jdom.Element;



import com.sap.sailing.domain.base.*;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;

import com.sap.sailing.domain.common.TimePoint;

import com.sap.sailing.domain.tracking.*;

import com.sap.sailing.server.RacingEventService;

public class EventDataPerLeg extends Action {
	public EventDataPerLeg(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
		super(req, res, service, maxRows);
	}

	public void perform() throws Exception {	
        		
        final Event event = getEvent(); // Get event data from request (get value for event name from URL parameter event)     
        // if the event does not exist a tag <message> will be returned with a text message from function getEvent(). 
        if(event == null){ 
        	return; 
        }
        
        final Document doc = new Document(); // initialize xml document
		final Element event_node = addNamedElement(doc,"event"); //add root to xml
    	addNamedElementWithValue(event_node, "name", event.getName());
    	addNamedElementWithValue(event_node, "boat_class", event.getBoatClass().getName());
        
        /*
         * Races
         * */
        final HashMap<String, RaceDefinition> races = getRaces(event); // get races for the event
        final Element races_node = addNamedElement(event_node,"races"); //add node that contains all races
        
        
        for (final RaceDefinition race : races.values()) { // for each race in the list        	        
        	final Element race_node = addNamedElement(races_node,"race"); //add race node for the current race
        	addNamedElementWithValue(race_node, "name", race.getName()); // add name node to current race
        	
        	//skip race if not tracked
        	final TrackedRace trackedRace = getTrackedRace(event, race);
	        if (trackedRace == null) {
	        	continue; 
	        } 
	        
            final TimePoint raceStarted = getTimePoint(trackedRace); // get TimePoint for when the race started    
            
            long minNextLegStart = raceStarted.asMillis(); //variable for keeping track of when the first competitor started the next leg
            TimePoint legStarted = new MillisecondsTimePoint(minNextLegStart); // get TimePoint for when the leg started
           
            
            addNamedElementWithValue(race_node, "start_time_ms", raceStarted.asMillis()); // add the starttime to the race
            addNamedElementWithValue(race_node, "assumed_end_ms", trackedRace.getAssumedEnd().asMillis()); // add the assumed enddtime
            
           
            
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
		                    addNamedElementWithValue(competitor_node, "leg_finished_time_ms", compFinishedLeg.asMillis());
		                    addNamedElementWithValue(competitor_node, "time_elapsed_ms", compFinishedLeg.asMillis() - raceStarted.asMillis());
		                    addNamedElementWithValue(competitor_node, "leg_time_ms", compLegTimeAlt.asMillis()); 
		                    addNamedElementWithValue(competitor_node, "leg_rank", trackedLegOfCompetitor.getRank(compFinishedLeg));		                    		                    
		                    addNamedElementWithValue(competitor_node, "rank_gain", posGL*-1); //Ranks Gained/Lost		                    
		                    addNamedElementWithValue(competitor_node, "gap_to_leader_s", trackedLegOfCompetitor.getGapToLeaderInSeconds(compFinishedLeg));
		                    addNamedElementWithValue(competitor_node, "ww_distance_to_leader_m", trackedLegOfCompetitor.getWindwardDistanceToOverallLeader(compFinishedLeg).getMeters());
		                    addNamedElementWithValue(competitor_node, "avg_speed_og_kn", trackedLegOfCompetitor.getAverageSpeedOverGround(compFinishedLeg).getKnots());
		                    addNamedElementWithValue(competitor_node, "avg_speed_ww_kn", trackedLegOfCompetitor.getAverageVelocityMadeGood(compFinishedLeg).getKnots());//windwardSpeed over ground on leg finished time
		                    addNamedElementWithValue(competitor_node, "distance_traveled_m", trackedLegOfCompetitor.getDistanceTraveled(compFinishedLeg).getMeters());
		                    addNamedElementWithValue(competitor_node, "number_of_jibes",  trackedLegOfCompetitor.getNumberOfJibes(compFinishedLeg));
		                    addNamedElementWithValue(competitor_node, "number_of_tacks",  trackedLegOfCompetitor.getNumberOfTacks(compFinishedLeg));
		                    addNamedElementWithValue(competitor_node, "number_of_penalty_circles",  trackedLegOfCompetitor.getNumberOfPenaltyCircles(compFinishedLeg));
		                    
		                             
		                    
		                    // assign the smallest start time for the next leg
		                    minNextLegStart = (minNextLegStart > compFinishedLeg.asMillis() ? compFinishedLeg.asMillis() : minNextLegStart);   
	                    }catch(Exception ex){	                    	
	                    	competitor_data_node.removeContent(competitor_node); // if the competitor dataset is not complete, remove it from the list
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
	        
        } // event end            
        sendDocument(doc, event.getName() + ".xml");// output doc to client
            
           
        
	} // function end
	
	

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
