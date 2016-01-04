package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.datamining.data.HasBackendPolarBoatClassContext;
import com.sap.sailing.polars.datamining.data.impl.BoatClassWithBackendPolarContext;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class BackendPolarsBoatClassRetrievalProcessor extends AbstractRetrievalProcessor<RacingEventService, HasBackendPolarBoatClassContext> {

    public BackendPolarsBoatClassRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasBackendPolarBoatClassContext, ?>> resultReceivers, int retrievalLevel) {
        super(RacingEventService.class, HasBackendPolarBoatClassContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasBackendPolarBoatClassContext> retrieveData(RacingEventService element) {
        PolarDataService polarDataService = element.getPolarDataService();
        return polarDataService.getAllBoatClassesWithPolarSheetsAvailable()
                .stream()
                .map(bc -> new BoatClassWithBackendPolarContext(bc, polarDataService))
                .collect(Collectors.toSet());
    }

}
