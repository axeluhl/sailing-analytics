package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map.Entry;

import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherSingle;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sse.common.Util;

public class StoreAndLoadDeviceConfigurationsTest extends AbstractMongoDBTest {

    public StoreAndLoadDeviceConfigurationsTest() throws UnknownHostException, MongoException {
        super();
    }

    protected MongoObjectFactoryImpl mongoFactory = (MongoObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getMongoObjectFactory(getMongoService());
    protected DomainObjectFactoryImpl domainFactory = (DomainObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);

    @Test
    public void testStoreEmptyConfiguration() {
        DeviceConfigurationMatcher matcher = new DeviceConfigurationMatcherSingle("");
        DeviceConfiguration configuration = new DeviceConfigurationImpl(new RegattaConfigurationImpl());
        mongoFactory.storeDeviceConfiguration(matcher, configuration);

        Iterable<Entry<DeviceConfigurationMatcher, DeviceConfiguration>> configurations = domainFactory
                .loadAllDeviceConfigurations();

        assertEquals(1, Util.size(configurations));
        Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry = configurations.iterator().next();
        assertNull(entry.getValue().getAllowedCourseAreaNames());
        assertNull(entry.getValue().getByNameCourseDesignerCourseNames());
        assertNull(entry.getValue().getResultsMailRecipient());
    }
    
    @Test
    public void testStoreConfiguration() {
        DeviceConfigurationMatcher matcher = new DeviceConfigurationMatcherSingle("");
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl(new RegattaConfigurationImpl());
        configuration.setAllowedCourseAreaNames(Arrays.asList("a","b"));
        configuration.setByNameDesignerCourseNames(Arrays.asList("a", "c"));
        configuration.setResultsMailRecipient("abc");
        mongoFactory.storeDeviceConfiguration(matcher, configuration);

        Iterable<Entry<DeviceConfigurationMatcher, DeviceConfiguration>> configurations = domainFactory
                .loadAllDeviceConfigurations();

        assertEquals(1, Util.size(configurations));
        Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry = configurations.iterator().next();
        assertNotNull(entry.getValue().getAllowedCourseAreaNames());
        assertEquals(2, entry.getValue().getAllowedCourseAreaNames().size());
        assertEquals(2, entry.getValue().getByNameCourseDesignerCourseNames().size());
        assertEquals(configuration.getByNameCourseDesignerCourseNames(), 
                entry.getValue().getByNameCourseDesignerCourseNames());
        assertEquals("abc", entry.getValue().getResultsMailRecipient());
    }
    
    @Test
    public void testRemoveConfiguration() {
        DeviceConfigurationMatcher matcher = new DeviceConfigurationMatcherSingle("");
        DeviceConfiguration configuration = new DeviceConfigurationImpl(new RegattaConfigurationImpl());
        mongoFactory.storeDeviceConfiguration(matcher, configuration);

        Iterable<Entry<DeviceConfigurationMatcher, DeviceConfiguration>> configurations = domainFactory
                .loadAllDeviceConfigurations();
        assertEquals(1, Util.size(configurations));
        mongoFactory.removeDeviceConfiguration(matcher);
        configurations = domainFactory.loadAllDeviceConfigurations();
        
        assertEquals(0, Util.size(configurations));
    }
}
