package com.sap.sailing.datamining;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.datamining.impl.data.SailingDataMiningClassesWithFunctionsService;
import com.sap.sse.datamining.ClassesWithFunctionsService;

public class Activator implements BundleActivator {
    
    public static final String dataRetrieverGroupName = "Sailing";

    private static BundleContext context;

    private ServiceRegistration<ClassesWithFunctionsService> classesWithFunctionsServiceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        registerClassesWithFunctionsService();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        unregisterClassesWithFunctionsService();
    }

    private void unregisterClassesWithFunctionsService() {
        context.ungetService(classesWithFunctionsServiceRegistration.getReference());
    }

    private void registerClassesWithFunctionsService() {
        classesWithFunctionsServiceRegistration = context.registerService(ClassesWithFunctionsService.class,
                new SailingDataMiningClassesWithFunctionsService(), null);
    }

}
