package com.sap.sse.datamining.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sap.sse.datamining.DataMiningPredefinedQueryService;

public abstract class AbstractDataMiningActivatorWithPredefinedQueries extends AbstractDataMiningActivator implements DataMiningPredefinedQueryService {
    
    private ServiceReference<DataMiningPredefinedQueryService> predefinedQueryServiceReference;
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        predefinedQueryServiceReference = context.registerService(DataMiningPredefinedQueryService.class, this, null).getReference();
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        context.ungetService(predefinedQueryServiceReference);
    }

}
