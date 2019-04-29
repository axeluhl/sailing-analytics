package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.datamining.data.HasBackendPolarBoatClassContext;
import com.sap.sailing.polars.datamining.data.impl.BoatClassWithBackendPolarContext;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class BackendPolarsBoatClassRetrievalProcessor extends AbstractRetrievalProcessor<RacingEventService, HasBackendPolarBoatClassContext> {

    public BackendPolarsBoatClassRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasBackendPolarBoatClassContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(RacingEventService.class, HasBackendPolarBoatClassContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasBackendPolarBoatClassContext> retrieveData(RacingEventService element) {
        Set<HasBackendPolarBoatClassContext> data = new HashSet<>();
        PolarDataService polarDataService = element.getPolarDataService();
        for (BoatClass boatClass : polarDataService.getAllBoatClassesWithPolarSheetsAvailable()) {
            if (isAborted()) {
                break;
            }
            data.add(new BoatClassWithBackendPolarContext(boatClass, polarDataService));
        }
        return data;
    }

}
