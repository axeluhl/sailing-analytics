package com.sap.sse.landscape.aws;

public interface LandscapeConstants {
    /**
     * The key <strong>tag</strong>, indicating that an instance only acts as a reverse proxy
     * (ie. it is not hosting other services). SO it can be terminated without risk.
     */
    String DISPOSABLE_PROXY = "DisposableProxy";
    
    /**
     * The tag name for a target group, which contains all the instances running httpd. Used to mark the
     * target groups containing all of the reverse proxies, disposable and otherwise. This allows for health checks
     * to be accessed and marks which groups any new reverse proxy instances should be added to.
     */
    String ALL_REVERSE_PROXIES = "allReverseProxies";
    
    /**
     * A string that all network load balancers contain.
     */
    String NLB_ARN_CONTAINS = "loadbalancer/net";
    
    /**
     * The value of "image-type" tag, which is used to identify which image to use to deploy a disposable reverse proxy.
     */
    String IMAGE_TYPE_REVERSE_PROXY = "disposable-reverse-proxy";
    
    /**
     * Indicates an instance is a reverse proxy.
     */
    String REVERSE_PROXY_TAG_NAME = "ReverseProxy";
    
    /**
     * The tag for a security group that a reverse proxy requires.
     */
    String REVERSE_PROXY_SG_TAG = "reverse-proxy-sg";
    
    /**
     * The tag for any security groups for a mongo DB.
     */
    String MONGO_SG_TAG = "mongo-sg";
    
    /**
     * The tag for a security group for an application server.
     */
    String SAILING_APPLICATION_SG_TAG = "application-server-sg";
    
    /**
     * Indicates that an instance is suitable for co-deployment with httpd.
     */
    String INSTANCE_SUITABLE_FOR_HTTPD = "canCoDeployWithHttpd";
    
    /**
     * The tag for the central reverse proxy, which also hosts non-essential services.
     */
    String CENTRAL_REVERSE_PROXY_TAG_NAME = "CentralReverseProxy";
}
