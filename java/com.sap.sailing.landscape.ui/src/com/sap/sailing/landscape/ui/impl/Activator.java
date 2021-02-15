package com.sap.sailing.landscape.ui.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.landscape.ui.shared.AwsSessionCredentialsFromUserPreference;
import com.sap.sse.security.interfaces.PreferenceConverter;
import com.sap.sse.security.util.GenericJSONPreferenceConverter;

public class Activator implements BundleActivator {
    private static BundleContext context;
    
    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        context.registerService(PreferenceConverter.class,
                new GenericJSONPreferenceConverter<>(() -> new AwsSessionCredentialsFromUserPreference()),
                /* properties */ null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    public static BundleContext getContext() {
        return context;
    }
}
