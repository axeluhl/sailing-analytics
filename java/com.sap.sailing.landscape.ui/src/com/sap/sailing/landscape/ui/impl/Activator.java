package com.sap.sailing.landscape.ui.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.landscape.ui.shared.AwsSessionCredentialsFromUserPreference;
import com.sap.sse.security.interfaces.PreferenceConverter;
import com.sap.sse.security.util.GenericJSONPreferenceConverter;

public class Activator implements BundleActivator {
    public static final String USER_PREFERENCE_FOR_SESSION_TOKEN = "___aws.session.token___";

    private static BundleContext context;
    
    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        final Hashtable<String, String> properties = new Hashtable<>();
        properties.put(PreferenceConverter.KEY_PARAMETER_NAME, USER_PREFERENCE_FOR_SESSION_TOKEN);
        context.registerService(PreferenceConverter.class,
                new GenericJSONPreferenceConverter<>(() -> new AwsSessionCredentialsFromUserPreference()),
                properties);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    public static BundleContext getContext() {
        return context;
    }
}
