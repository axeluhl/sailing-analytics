package com.sap.sailing.ingestion;

import software.amazon.awssdk.regions.Region;

public interface Configuration {
    /**
     * This host is only reachable through VPC and therefore one needs to ensure that the Lambda has all relevant
     * permissions
     */
    String[] REDIS_ENDPOINTS = { "redis://fixingestionrediscache-serverless-cvoblp.serverless.euw2.cache.amazonaws.com:6379" };
    String REDIS_MAP_NAME = "endpoints";

    Region S3_REGION = Region.EU_WEST_2;
    String S3_BUCKET_NAME = "sapsailing-gps-fixes";

    int TIMEOUT_IN_SECONDS_WHEN_DISPATCHING_TO_ENDPOINT = 3;
}
