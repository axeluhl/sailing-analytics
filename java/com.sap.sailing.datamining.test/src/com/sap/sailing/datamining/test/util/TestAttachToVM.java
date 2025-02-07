package com.sap.sailing.datamining.test.util;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import net.bytebuddy.agent.VirtualMachine;

/**
 * Requires com.sun.jna, com.sun.jna.platform bundles and the net.bytebuddy stuff to be on the classpath.
 * 
 * @author Axel Uhl (d043530)
 *
 */
@Ignore("This class is used only to demonstrate how injecting the JMX Prometheus Java agent works for a running VM")
public class TestAttachToVM {
    @Test
    public void testInjectAgentToRunningVM() throws IOException {
        final VirtualMachine vm = VirtualMachine.ForHotSpot.attach("414179");
        vm.loadAgent("/path/to/jmx_prometheus_javaagent-1.1.0.jar", "9000:/path/to/java/jmxPrometheusConfig.yaml");
        assertNotNull(vm);
        vm.detach();
    }
}
