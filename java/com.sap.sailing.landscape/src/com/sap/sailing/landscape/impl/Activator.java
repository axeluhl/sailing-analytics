package com.sap.sailing.landscape.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.landscape.LandscapeService;
import com.sap.sse.security.interfaces.PreferenceConverter;
import com.sap.sse.security.util.GenericJSONPreferenceConverter;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(LandscapeService.class, new LandscapeServiceImpl(context), /* properties */ null);
        final Hashtable<String, String> properties = new Hashtable<>();
        properties.put(PreferenceConverter.KEY_PARAMETER_NAME, LandscapeService.USER_PREFERENCE_FOR_SESSION_TOKEN);
        context.registerService(PreferenceConverter.class,
                new GenericJSONPreferenceConverter<>(() -> new AwsSessionCredentialsFromUserPreference()),
                properties);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
