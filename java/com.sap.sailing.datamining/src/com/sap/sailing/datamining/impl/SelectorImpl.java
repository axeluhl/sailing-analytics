package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.SelectionCriteria;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.server.RacingEventService;

public class SelectorImpl implements Selector {
    
    private SelectionCriteria criteria;

    public SelectorImpl(SelectionCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public List<GPSFixWithContext> selectGPSFixes(RacingEventService racingEventService) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (Regatta regatta : racingEventService.getAllRegattas()) {
            TrackedRegatta trackedRegatta = racingEventService.getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                SelectionContext context = new SelectionContextImpl(trackedRegatta);
                if (criteria.matches(context)) {
                    DataRetriever dataRetriever = new TrackedRegattaDataRetriever(trackedRegatta);
                    data.addAll(dataRetriever.retrieveData());
                    continue;
                }
                
                for (TrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
                    context.setTrackedRace(trackedRace);
                    if (criteria.matches(context)) {
                        DataRetriever dataRetriever = new TrackedRaceDataRetriever(trackedRace);
                        data.addAll(dataRetriever.retrieveData());
                        continue;
                    }
                    
                    for (TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
                        context.setTrackedLeg(trackedLeg);
                        if (criteria.matches(context)) {
                            //TODO Create new data retriever and add the data to the result
                            continue;
                        }
                    }
                    
                    for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                        context.setCompetitor(competitor);
                        if (criteria.matches(context)) {
                            //TODO Create new data retriever and add the data to the result
                            continue;
                        }
                        
                        for (TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
                            context.setTrackedLeg(trackedLeg);
                            if (criteria.matches(context)) {
                                //TODO Create new data retriever and add the data to the result
                                continue;
                            }
                        }
                    }
                }
            }
        }
        return data;
    }

}
