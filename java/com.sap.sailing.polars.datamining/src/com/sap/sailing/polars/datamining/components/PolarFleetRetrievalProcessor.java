package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.polars.datamining.data.HasFleetPolarContext;
import com.sap.sailing.polars.datamining.data.HasRaceColumnPolarContext;
import com.sap.sailing.polars.datamining.data.impl.FleetWithPolarContext;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class PolarFleetRetrievalProcessor extends AbstractRetrievalProcessor<HasRaceColumnPolarContext, HasFleetPolarContext> {

    public PolarFleetRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasFleetPolarContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasRaceColumnPolarContext.class, HasFleetPolarContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasFleetPolarContext> retrieveData(HasRaceColumnPolarContext element) {
        Set<HasFleetPolarContext> fleetWithContext = new HashSet<>();
        RaceColumn raceColumn = element.getRaceColumn();
        for (Fleet fleet : raceColumn.getFleets()) {
            if (isAborted()) {
                break;
            }
            fleetWithContext.add(new FleetWithPolarContext(fleet, raceColumn, element));
        }
        return fleetWithContext;
    }

}
