package com.sap.sailing.landscape.common;

public interface SharedLandscapeConstants {
    /**
     * If no specific domain name is provided, e.g., when creating a new application replica set, this will be
     * the default domain name appended to the host name which, in turn, may be derived, e.g., from the application
     * replica set's name.
     */
    String DEFAULT_DOMAIN_NAME = "sapsailing.com";
    
    /**
     * If a shared security realm is to be used for a domain then this constant tells the name of the application
     * replica set that by default manages the shared security information. Other replicables that are to be shared
     * through the same realm, such as landscape management data or shared sailing data such as course templates or mark
     * properties inventories, can be shared from the same application replica set.
     * <p>
     * 
     * To obtain the full host name used by the application replica set, append "." and the
     * {@link #DEFAULT_DOMAIN_NAME}.
     */
    String DEFAULT_SECURITY_SERVICE_REPLICA_SET_NAME = "security-service";
    
    /**
     * This is the region of the load balancer handling the default traffic for {@code *.sapsailing.com}. It is also
     * called the "dynamic" load balancer because adding, removing or changing any hostname-based rule in its HTTPS
     * listener's rule set takes effect immediately and is hence suited well for short-lived events that will be
     * archived after a short period of time.<p>
     * 
     * Care must be taken not to attempt a "dynamic" load balancer set-up for replica sets launched in regions
     * other than the one identified by this region constant because otherwise the {@code *.sapsailing.com} default
     * Route53 DNS record would be adjusted to point to that region's dynamic load balancer instead, making the
     * actual default load balancer and its default rule routing to the central reverse proxy and from there to
     * the landing page and the archive server inactive.<p>
     * 
     * A future set-up may look different, though, with "dynamic" load balancers grouped in an AWS Global Accelerator
     * which handles cross-region traffic automatically, based on where load balancers are available. Archive servers
     * may be replicated into multiple regions, and so may reverse proxy configurations that handle re-write rules
     * for archived events. If such a state is reached, "dynamic" load balancing may potentially be used regardless
     * the region.
     */
    String REGION_WITH_DEFAULT_LOAD_BALANCER = "eu-west-1";

    /**
     * The tag value used to identify host images that can be launched in order to run one or more Sailing Analytics
     * server processes on it.
     */
    String IMAGE_TYPE_TAG_VALUE_SAILING = "sailing-analytics-server";
    
    /**
     * The tag attached to hosts running zero or more Sailing Analytics processes. Can be used to discover
     * application replica sets in a landscape.
     */
    String SAILING_ANALYTICS_APPLICATION_HOST_TAG = "sailing-analytics-server";

    String ARCHIVE_SERVER_APPLICATION_REPLICA_SET_NAME = "ARCHIVE";

    /**
     * Value of the {@link #SAILING_ANALYTICS_APPLICATION_HOST_TAG} tag
     * for hosts expected to run more than one dedicated application process.
     */
    String MULTI_PROCESS_INSTANCE_TAG_VALUE = "___multi___";

    /**
     * Default value for the {@code Name} tag for shared instances expected to run multiple application processes.
     */
    String MULTI_PROCESS_INSTANCE_DEFAULT_NAME = "SL Multi-Server";

    String DEFAULT_DEDICATED_INSTANCE_TYPE_NAME = "C4_2_XLARGE";
    
    String DEFAULT_SHARED_INSTANCE_TYPE_NAME = "I3_2_XLARGE";

    /**
     * Tells how to size process heaps on shared instances by default, based on the instance's physical memory.
     * Harmonizes with the {@link #DEFAULT_SHARED_INSTANCE_TYPE_NAME} and the expected approximate memory requirements
     * of a typical process instance.
     */
    int DEFAULT_NUMBER_OF_PROCESSES_IN_MEMORY = 4;
    
    /**
     * Indicates that an instance only acts as a reverse proxy. ie. it is not hosting other services. SO it can be
     * terminated without risk.
     */
    String DISPOSABLE_PROXY = "DisposableProxy";
    
    /**
     * The tag name for a target group which contains all the instances running httpd. Used to mark the
     * target groups containing all of the reverse proxies, disposable and otherwise. This allows for health checks
     * to be accessed and marks which groups any new reverse proxy instances should be added to.
     */
    String ALL_REVERSE_PROXIES = "allReverseProxies";
    
    /**
     * 
     */
    String NLB_ARN_CONTAINS = "loadbalancer/net";
    /**
     * Used to launch the correct ami.
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
     * Marks the most appropriate instance type for a reverse proxy.
     */
    String DEFAULT_REVERSE_PROXY_INSTANCE_TYPE = "C5_AD_XLARGE";
    
    /**
     * The tag for any security groups for a mongo DB.
     */
    String MONGO_SG_TAG = "mongo-sg";
    
    /**
     * The tag for a security group for a sailing application server.
     */
    String SAILING_APPLICATION_SG_TAG = "application-server-sg";
    /**
     * Indicates that an instance is suitable for co-deployment with httpd.
     */
    String INSTANCE_SUITABLE_FOR_HTTPD = "canCoDeployWithHttpd";
    
    /**
     * The tag for the central reverse proxy, which hosts non-essential services.
     */
    String CENTRAL_REVERSE_PROXY_TAG_NAME = "CentralReverseProxy";
}
