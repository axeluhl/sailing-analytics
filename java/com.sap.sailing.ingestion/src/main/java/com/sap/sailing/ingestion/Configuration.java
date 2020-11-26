package com.sap.sailing.ingestion;

import software.amazon.awssdk.regions.Region;

public class Configuration {
    /**
     * This host is only reachable through VPC and therefore one needs to ensure that the Lambda has all relevant
     * permissions
     */
    protected static final String MEMCACHED_ENDPOINT_HOST = "fixingestion.cvoblp.cfg.euw2.cache.amazonaws.com";
    protected static final int MEMCACHED_ENDPOINT_PORT = 11211;

    protected static final Region S3_REGION = Region.EU_WEST_2;
    protected static final String S3_BUCKET_NAME = "sapsailing-gps-fixes";
}
