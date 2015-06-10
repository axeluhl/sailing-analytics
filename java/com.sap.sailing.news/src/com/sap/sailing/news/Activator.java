package com.sap.sailing.news;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.news.impl.NewsProviderRegistryImpl;
import com.sap.sailing.news.impl.NewsServiceImpl;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());

    public void start(BundleContext context) throws Exception {
        NewsProviderRegistry providerRegistry = new NewsProviderRegistryImpl(); 
        NewsService newsService = new NewsServiceImpl(providerRegistry);
        context.registerService(NewsService.class, newsService, null);
        context.registerService(NewsProviderRegistry.class, providerRegistry, null);
        logger.info("News Service registered.");
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
