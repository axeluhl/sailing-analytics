package com.sap.sailing.domain.persistence.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.persistence.CollectionNameService;

public class Activator implements BundleActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		CollectionNameService service = new CollectionNameServiceImpl();
		context.registerService(CollectionNameService.class, service, null);
		for (CollectionNames name : CollectionNames.values())
			service.registerExclusively(CollectionNames.class, name.name());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
