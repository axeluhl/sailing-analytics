package com.sap.sailing.news;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.news.impl.EventNewsProviderRegistryImpl;
import com.sap.sailing.news.impl.EventNewsServiceImpl;
import com.sap.sailing.news.impl.LeaderboardUpdateEventNewsProvider;
import com.sap.sailing.news.impl.SimpleDemoEventNewsProvider;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());

    public void start(BundleContext context) throws Exception {
        EventNewsProviderRegistry providerRegistry = new EventNewsProviderRegistryImpl(); 
        EventNewsService newsService = new EventNewsServiceImpl(providerRegistry);
        context.registerService(EventNewsService.class, newsService, null);
        context.registerService(EventNewsProviderRegistry.class, providerRegistry, null);
        
        providerRegistry.registerNewsProvider(new SimpleDemoEventNewsProvider());
        providerRegistry.registerNewsProvider(new LeaderboardUpdateEventNewsProvider());
        logger.info("EventNews Service registered.");
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
