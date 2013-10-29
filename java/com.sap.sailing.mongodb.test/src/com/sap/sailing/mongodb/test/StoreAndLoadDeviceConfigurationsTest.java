package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map.Entry;

import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherAny;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;

public class StoreAndLoadDeviceConfigurationsTest extends AbstractMongoDBTest {

    public StoreAndLoadDeviceConfigurationsTest() throws UnknownHostException, MongoException {
        super();
    }

    protected MongoObjectFactoryImpl mongoFactory = (MongoObjectFactoryImpl) MongoFactory.INSTANCE
            .getMongoObjectFactory(getMongoService());
    protected DomainObjectFactoryImpl domainFactory = (DomainObjectFactoryImpl) MongoFactory.INSTANCE
            .getDomainObjectFactory(getMongoService());

    @Test
    public void testStoreEmptyConfiguration() {
        DeviceConfigurationMatcher matcher = DeviceConfigurationMatcherAny.INSTANCE;
        DeviceConfiguration configuration = new DeviceConfigurationImpl();
        mongoFactory.storeDeviceConfiguration(matcher, configuration);

        Iterable<Entry<DeviceConfigurationMatcher, DeviceConfiguration>> configurations = domainFactory
                .loadAllDeviceConfigurations();

        assertEquals(1, Util.size(configurations));
        Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry = configurations.iterator().next();
        assertEquals(matcher, entry.getKey());
        assertNull(entry.getValue().getAllowedCourseAreaNames());
        assertNull(entry.getValue().getMinimumRoundsForCourse());
        assertNull(entry.getValue().getMaximumRoundsForCourse());
        assertNull(entry.getValue().getResultsMailRecipient());
    }
    
    @Test
    public void testStoreConfiguration() {
        DeviceConfigurationMatcher matcher = DeviceConfigurationMatcherAny.INSTANCE;
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl();
        configuration.setAllowedCourseAreaNames(Arrays.asList("a","b"));
        configuration.setMinimumRoundsForCourse(4);
        configuration.setMaximumRoundsForCourse(5);
        configuration.setResultsMailRecipient("abc");
        mongoFactory.storeDeviceConfiguration(matcher, configuration);

        Iterable<Entry<DeviceConfigurationMatcher, DeviceConfiguration>> configurations = domainFactory
                .loadAllDeviceConfigurations();

        assertEquals(1, Util.size(configurations));
        Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry = configurations.iterator().next();
        assertEquals(matcher, entry.getKey());
        assertNotNull(entry.getValue().getAllowedCourseAreaNames());
        assertEquals(2, entry.getValue().getAllowedCourseAreaNames().size());
        assertEquals(new Integer(4), entry.getValue().getMinimumRoundsForCourse());
        assertEquals(new Integer(5), entry.getValue().getMaximumRoundsForCourse());
        assertEquals("abc", entry.getValue().getResultsMailRecipient());
    }
    
    @Test
    public void testRemoveConfiguration() {
        DeviceConfigurationMatcher matcher = DeviceConfigurationMatcherAny.INSTANCE;
        DeviceConfiguration configuration = new DeviceConfigurationImpl();
        mongoFactory.storeDeviceConfiguration(matcher, configuration);

        Iterable<Entry<DeviceConfigurationMatcher, DeviceConfiguration>> configurations = domainFactory
                .loadAllDeviceConfigurations();
        assertEquals(1, Util.size(configurations));
        mongoFactory.removeDeviceConfiguration(matcher);
        configurations = domainFactory.loadAllDeviceConfigurations();
        
        assertEquals(0, Util.size(configurations));
    }
}
