package com.tractrac.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.tractrac.dataflow.lib.api.DataflowLocator;
import com.tractrac.dataflow.lib.impl.DefaultDataProviderFactory;
import com.tractrac.dataflow.lib.prov.live.tcp.provider.TCPLiveProviderFactory;
import com.tractrac.dataflow.lib.prov.stored.mtb.MTBStoredDataProviderFactory;
import com.tractrac.dataflow.lib.prov.stored.tcp.TCPStoredProviderFactory;
import com.tractrac.dataflow.lib.spi.DataflowProviderLocator;
import com.tractrac.model.lib.api.ModelLocator;
import com.tractrac.model.lib.impl.attachment.DefaultAttachmentManager;
import com.tractrac.model.lib.impl.data.DefaultPositionFactory;
import com.tractrac.model.lib.impl.event.EventFactory;
import com.tractrac.model.lib.impl.metadata.DefaultMetadataFactory;
import com.tractrac.model.lib.impl.route.PathRouteFactory;
import com.tractrac.subscription.lib.api.SubscriptionLocator;
import com.tractrac.subscription.lib.impl.SubscriberFactory;
import com.tractrac.util.lib.api.UtilLocator;
import com.tractrac.util.lib.api.autolog.LoggerLocator;
import com.tractrac.util.lib.impl.autolog.DefaultLoggerManager;
import com.tractrac.util.lib.impl.programparameters.DefaultParameterSetFactory;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        LoggerLocator.registerParameterSetFactory(new DefaultLoggerManager());
        UtilLocator.registerParameterSetFactory(new DefaultParameterSetFactory());
        DataflowLocator.registerDataProviderFactory(new DefaultDataProviderFactory());
        DataflowProviderLocator.registerLiveDataProviderFactory(new TCPLiveProviderFactory());
        DataflowProviderLocator.registerStoredDataProviderFactory(new TCPStoredProviderFactory());
        DataflowProviderLocator.registerStoredDataProviderFactory(new MTBStoredDataProviderFactory());
        ModelLocator.registerAttachmentManager(new DefaultAttachmentManager());
        ModelLocator.registerEventFactory(new EventFactory());
        ModelLocator.registerPathRouteFactory(new PathRouteFactory());
        ModelLocator.registerPositionFactory(new DefaultPositionFactory());
        ModelLocator.registerMetadataFactory(new DefaultMetadataFactory());
        SubscriptionLocator.registerSubscriberFactory(new SubscriberFactory());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
