package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherSingle;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;

public class DeviceConfigurationReplicationTest extends AbstractServerReplicationTest {

    @Test
    public void testCreateConfiguration() throws InterruptedException {
        DeviceConfigurationMatcher matcher = new DeviceConfigurationMatcherSingle("a");
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl(new RegattaConfigurationImpl());
        master.createOrUpdateDeviceConfiguration(matcher, configuration);
        Thread.sleep(1000);
        Map<DeviceConfigurationMatcher, DeviceConfiguration> configurationMap = replica.getAllDeviceConfigurations();
        assertEquals(1, configurationMap.size());
        assertEquals(matcher.getMatcherType(), configurationMap.keySet().iterator().next().getMatcherType());
    }
    
    @Test
    public void testUpdateConfiguration() throws InterruptedException {
        DeviceConfigurationMatcher matcher = new DeviceConfigurationMatcherSingle("23");
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl(new RegattaConfigurationImpl());
        
        configuration.setAllowedCourseAreaNames(Arrays.asList("hallo"));
        master.createOrUpdateDeviceConfiguration(matcher, configuration);
        configuration.setAllowedCourseAreaNames(Arrays.asList("hallo", "welt"));
        master.createOrUpdateDeviceConfiguration(matcher, configuration);
        Thread.sleep(1000);
        
        Map<DeviceConfigurationMatcher, DeviceConfiguration> configurationMap = replica.getAllDeviceConfigurations();
        assertEquals(1, configurationMap.size());
        
        DeviceConfiguration replicatedConfiguration = configurationMap.values().iterator().next();
        assertEquals(configuration.getByNameCourseDesignerCourseNames(), 
                replicatedConfiguration.getByNameCourseDesignerCourseNames());
    }
    
    @Test
    public void testRemoveConfiguration() throws InterruptedException {
        DeviceConfigurationMatcher matcher = new DeviceConfigurationMatcherSingle("24");
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl(new RegattaConfigurationImpl());
        
        master.createOrUpdateDeviceConfiguration(matcher, configuration);
        Thread.sleep(1000);
        master.removeDeviceConfiguration(matcher);
        Thread.sleep(1000);
        
        Map<DeviceConfigurationMatcher, DeviceConfiguration> configurationMap = replica.getAllDeviceConfigurations();
        assertEquals(0, configurationMap.size());
    }
}
