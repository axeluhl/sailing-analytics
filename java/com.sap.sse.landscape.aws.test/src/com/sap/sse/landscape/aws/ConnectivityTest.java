package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.impl.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.impl.AwsInstance;
import com.sap.sse.landscape.aws.impl.AwsRegion;

import software.amazon.awssdk.regions.Region;

public class ConnectivityTest {
    private AwsLandscape<String, ApplicationProcessMetrics> landscape;
    private AwsRegion region;
    
    @Before
    public void setUp() {
        landscape = AwsLandscape.obtain();
        region = new AwsRegion(Region.EU_WEST_2);
    }
    
    @Test
    public void testConnectivity() {
        final AwsInstance host = landscape.launchHost(landscape.getImage(region, "ami-01b4b27a5699e33e6"),
                new AwsAvailabilityZone("eu-west-2b", region), Collections.singleton(()->"sg-0b2afd48960251280"));
        try {
            assertNotNull(host);
        } finally {
            landscape.terminate(host);
        }
    }
    
    @Test
    public void testImageDate() throws ParseException {
        final AmazonMachineImage image = landscape.getImage(region, "ami-01b4b27a5699e33e6");
        assertEquals(TimePoint.of(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").parse("2020-07-08T12:41:06+0200")),
                image.getCreatedAt());
    }
}
