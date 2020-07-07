package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

public interface AWSLandscape<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends Landscape<ShardingKey, MetricsT> {
    static String ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME = "com.sap.sse.landscape.aws.accesskeyid";

    static String SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME = "com.sap.sse.landscape.aws.secretaccesskey";

    /**
     * Based on system properties for the AWS access key ID and the secret access key, this method
     * returns a landscape object which internally has access to the clients for the underlying AWS landscape,
     * such as an EC2 client, a Route53 client, etc.
     */
    static <ShardingKey, MetricsT extends ApplicationProcessMetrics> AWSLandscape<ShardingKey, MetricsT> obtain() {
        return null; // TODO
    }
}
