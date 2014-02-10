package com.sap.sailing.domain.persistence.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.persistence.devices.GPSFixPersistenceHandler;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.mongodb.MongoDBService;

public class Activator implements BundleActivator {
    private Set<ServiceRegistration<?>> registrations = new HashSet<>();
	@Override
	public void start(BundleContext context) throws Exception {
		for (CollectionNames name : CollectionNames.values())
			MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
		
		MongoObjectFactoryImpl mof = new MongoObjectFactoryImpl(null);
		DomainObjectFactoryImpl dof = new DomainObjectFactoryImpl(null, null);
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(TypeBasedServiceFinder.TYPE, GPSFixImpl.class.getName());
        registrations.add(context.registerService(GPSFixPersistenceHandler.class,
        		new GPSFixPersistenceHandlerImpl(mof, dof), properties));
        properties.put(TypeBasedServiceFinder.TYPE, GPSFixMovingImpl.class.getName());
        registrations.add(context.registerService(GPSFixPersistenceHandler.class,
        		new GPSFixMovingPersistenceHandlerImpl(mof, dof), properties));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
	}

}
