package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherAny;

public class DeviceConfigurationReplicationTest extends AbstractServerReplicationTest {

    @Test
    public void testEventReplication() throws InterruptedException {
        DeviceConfigurationMatcher matcher = DeviceConfigurationMatcherAny.INSTANCE;
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl();
        master.createOrUpdateDeviceConfiguration(matcher, configuration);
        
        Thread.sleep(1000);
        Map<DeviceConfigurationMatcher, DeviceConfiguration> configurationMap = replica.getAllDeviceConfigurations();
        assertEquals(1, configurationMap.size());
        assertEquals(matcher.getMatcherType(), configurationMap.keySet().iterator().next().getMatcherType());
    }
}
