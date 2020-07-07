package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.impl.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.impl.AwsInstance;
import com.sap.sse.landscape.aws.impl.AwsRegion;

import software.amazon.awssdk.regions.Region;

public class ConnectivityTest {
    private AwsLandscape<String, ApplicationProcessMetrics> landscape;
    
    @Before
    public void setUp() {
        landscape = AwsLandscape.obtain();
    }
    
    @Test
    public void testConnectivity() {
        final AwsRegion euWest3 = new AwsRegion(Region.EU_WEST_3);
        final AwsInstance host = landscape.launchHost(landscape.getImage(euWest3, "ami-02a74a4e5b2e5988c"),
                new AwsAvailabilityZone("eu-west-3c", euWest3), Collections.singleton(()->"sg-0931783464106ed69"));
        try {
            assertNotNull(host);
        } finally {
            landscape.terminate(host);
        }
    }
}
