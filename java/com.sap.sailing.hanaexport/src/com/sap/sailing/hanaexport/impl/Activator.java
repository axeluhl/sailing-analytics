package com.sap.sailing.hanaexport.impl;

import java.sql.DriverManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.db.jdbc.Driver;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        DriverManager.registerDriver(Driver.singleton());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
