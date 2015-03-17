package com.sap.sse.datamining.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sap.sse.datamining.DataMiningBundleService;

public abstract class AbstractDataMiningActivator implements BundleActivator {

    private ServiceReference<DataMiningBundleService> dataMiningBundleServiceReference;
    
    @Override
    public void start(BundleContext context) throws Exception {
        dataMiningBundleServiceReference = context.registerService(DataMiningBundleService.class, getDataMiningBundleService(), null).getReference();
    }

    protected abstract DataMiningBundleService getDataMiningBundleService();

    @Override
    public void stop(BundleContext context) throws Exception {
        context.ungetService(dataMiningBundleServiceReference);
    }

}
