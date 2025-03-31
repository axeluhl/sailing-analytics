package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.aws.impl.AwsRegion;

public interface AwsAvailabilityZone extends AvailabilityZone {
    @Override
    AwsRegion getRegion();
}
