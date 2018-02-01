package com.sap.sailing.nmeaconnector.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.nmeaconnector.NmeaFactory;

import net.sf.marineapi.nmea.parser.SentenceFactory;

/**
 * Registers and unregisters additional proprietary NMEA sentence parsers with the {@link SentenceFactory}
 * {@link SentenceFactory#getInstance() default instance}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        NmeaFactory.INSTANCE.getUtil().registerAdditionalParsers();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        NmeaFactory.INSTANCE.getUtil().unregisterAdditionalParsers();
    }
}
