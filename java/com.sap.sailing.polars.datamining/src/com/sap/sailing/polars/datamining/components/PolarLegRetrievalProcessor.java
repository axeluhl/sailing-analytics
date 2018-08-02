package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasFleetPolarContext;
import com.sap.sailing.polars.datamining.data.HasLegPolarContext;
import com.sap.sailing.polars.datamining.data.impl.LegWithPolarContext;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class PolarLegRetrievalProcessor extends AbstractRetrievalProcessor<HasFleetPolarContext, HasLegPolarContext> {

    public PolarLegRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasLegPolarContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasFleetPolarContext.class, HasLegPolarContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasLegPolarContext> retrieveData(HasFleetPolarContext element) {
        TrackedRace trackedRace = element.getTrackedRace();
        Set<HasLegPolarContext> legWithContext = new HashSet<>();
        Fleet fleet = element.getFleet();
        RaceColumn raceColumn = element.getRaceColumn();
        
        RaceDefinition raceDefinition = raceColumn.getRaceDefinition(fleet);
        if (raceDefinition != null) {
            Course course = raceDefinition.getCourse();
            for (Leg leg : course.getLegs()) {
                if (isAborted()) {
                    break;
                }
                legWithContext.add(new LegWithPolarContext(leg, trackedRace, element));
            }
        } 
        return legWithContext;
    }

}
