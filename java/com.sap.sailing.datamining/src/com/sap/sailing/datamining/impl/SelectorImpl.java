package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
                SelectionContext context = new SelectionContextImpl(trackedRegatta, null, null, null);
                if (criteria.matches(context)) {
                    data.addAll(criteria.getDataRetriever(context).retrieveData());
                    continue;
                }
                
                data.addAll(selectDataFromRacesOfRegatta(trackedRegatta));
            }
        }
        return data;
    }

    private Collection<GPSFixWithContext> selectDataFromRacesOfRegatta(final TrackedRegatta trackedRegatta) {
        Collection<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (TrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
            SelectionContext context = new SelectionContextImpl(trackedRegatta, trackedRace, null, null);
            if (criteria.matches(context)) {
                //TODO Create new data retriever and add the data to the result
                continue;
            }
            
            data.addAll(selectDataFromLegsOfRace(trackedRegatta, trackedRace));
            
            data.addAll(selectDataFromCompetitorsOfRace(trackedRegatta, trackedRace));
        }
        return data;
    }

    private Collection<GPSFixWithContext> selectDataFromCompetitorsOfRace(final TrackedRegatta trackedRegatta, final TrackedRace trackedRace) {
        Collection<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            SelectionContext context = new SelectionContextImpl(trackedRegatta, trackedRace, competitor, null);
            if (criteria.matches(context)) {
                //TODO Create new data retriever and add the data to the result
                continue;
            }
            
            for (TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
                context = new SelectionContextImpl(trackedRegatta, trackedRace, competitor, trackedLeg);
                if (criteria.matches(context)) {
                    //TODO Create new data retriever and add the data to the result
                    continue;
                }
            }
        }
        return data;
    }

    private Collection<GPSFixWithContext> selectDataFromLegsOfRace(final TrackedRegatta trackedRegatta, final TrackedRace trackedRace) {
        Collection<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
            SelectionContext context = new SelectionContextImpl(trackedRegatta, trackedRace, null, trackedLeg);
            if (criteria.matches(context)) {
                //TODO Create new data retriever and add the data to the result
                continue;
            }
        }
        return data;
    }

}
