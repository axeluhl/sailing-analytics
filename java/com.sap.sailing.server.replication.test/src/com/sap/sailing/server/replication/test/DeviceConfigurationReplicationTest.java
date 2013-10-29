package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherAny;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherSingle;

public class DeviceConfigurationReplicationTest extends AbstractServerReplicationTest {

    @Test
    public void testCreateConfiguration() throws InterruptedException {
        DeviceConfigurationMatcher matcher = DeviceConfigurationMatcherAny.INSTANCE;
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl();
        
        master.createOrUpdateDeviceConfiguration(matcher, configuration);
        Thread.sleep(1000);
        
        Map<DeviceConfigurationMatcher, DeviceConfiguration> configurationMap = replica.getAllDeviceConfigurations();
        assertEquals(1, configurationMap.size());
        assertEquals(matcher.getMatcherType(), configurationMap.keySet().iterator().next().getMatcherType());
    }
    
    @Test
    public void testUpdateConfiguration() throws InterruptedException {
        DeviceConfigurationMatcher matcher = new DeviceConfigurationMatcherSingle("23");
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl();
        
        configuration.setMinimumRoundsForCourse(2);
        master.createOrUpdateDeviceConfiguration(matcher, configuration);
        configuration.setMinimumRoundsForCourse(42);
        master.createOrUpdateDeviceConfiguration(matcher, configuration);
        Thread.sleep(1000);
        
        Map<DeviceConfigurationMatcher, DeviceConfiguration> configurationMap = replica.getAllDeviceConfigurations();
        assertEquals(1, configurationMap.size());
        assertEquals(configuration.getMinimumRoundsForCourse(), configurationMap.values().iterator().next().getMinimumRoundsForCourse());
    }
    
    @Test
    public void testRemoveConfiguration() throws InterruptedException {
        DeviceConfigurationMatcher matcher = new DeviceConfigurationMatcherSingle("24");
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl();
        
        master.createOrUpdateDeviceConfiguration(matcher, configuration);
        Thread.sleep(1000);
        master.removeDeviceConfiguration(matcher);
        Thread.sleep(1000);
        
        Map<DeviceConfigurationMatcher, DeviceConfiguration> configurationMap = replica.getAllDeviceConfigurations();
        assertEquals(0, configurationMap.size());
    }
}
