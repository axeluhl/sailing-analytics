package com.sap.sailing.aiagent.persistence;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.aiagent.interfaces.AIAgent;
import com.sap.sailing.aiagent.interfaces.AIAgentListener;
import com.sap.sailing.aiagent.persistence.impl.CollectionNames;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.util.ServiceTrackerFactory;

import com.sap.sailing.aiagent.persistence.impl.CollectionNames;
import com.sap.sse.mongodb.MongoDBService;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        for (CollectionNames name : CollectionNames.values()) {
            MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
        }
        new Thread(()->{
            final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(bundleContext, RacingEventService.class);
            final ServiceTracker<AIAgent, AIAgent> aiAgentTracker = ServiceTrackerFactory.createAndOpen(bundleContext, AIAgent.class);
            try {
                final AIAgent aiAgent = aiAgentTracker.waitForService(0);
                final RacingEventService racingEventService = racingEventServiceTracker.waitForService(0);
                logger.info("Found service "+aiAgent);
                aiAgent.stopCommentingOnAllEvents();
                final DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
                final MongoObjectFactory mof = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory();
                for (final Event eventToCommentOn : racingEventService.getEventsSelectively(/* include */ true, dof.getEventsToComment(/* clientSessionOrNull */ null))) {
                    aiAgent.startCommentingOnEvent(eventToCommentOn);
                }
                aiAgent.addListener(new AIAgentListener() {
                    @Override
                    public void stoppedCommentingOnEvent(Event e) {
                        mof.removeEventToComment(e.getId(), /* clientSessionOrNull */ null);
                    }
        Activator.context = bundleContext;
    }

                    @Override
                    public void startedCommentingOnEvent(Event e) {
                        mof.addEventToComment(e.getId(), /* clientSessionOrNull */ null);
                    }
                });
            } catch (InterruptedException e) {
                logger.severe("Got interrupted while waiting for AIAgent service");
                throw new RuntimeException(e);
            }
        }, "Waiting for AIAgent to show up in OSGi registry").start();
        Activator.context = bundleContext;
    }

    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }
}
