package com.sap.sse.landscape;

import java.util.Optional;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

public interface Landscape<ShardingKey> {
    /**
     * The {@link Landscape#getLatestImageWithTag(Region, String, String)} method is
     * used to obtain default images for specific host starting procedures that subclass this class. The
     * Machine Images for this are then expected to be tagged with a tag named as specified by this
     * constant ("image-type"). The tag value then must match what the subclass wants.
     */
    String IMAGE_TYPE_TAG_NAME = "image-type";
    /**
     * The timeout for a host to come up
     */
    Optional<Duration> WAIT_FOR_HOST_TIMEOUT = Optional.of(Duration.ONE_MINUTE.times(30));
    /**
     * The timeout for a running process to respond
     */
    Optional<Duration> WAIT_FOR_PROCESS_TIMEOUT = Optional.of(Duration.ONE_MINUTE);

    /**
     * @return the security group that shall be assigned by default to any application server host, whether master or
     *         replica
     */
    SecurityGroup getDefaultSecurityGroupForApplicationHosts(Region region);
    
    /**
     * @return the security group that shall be assigned by default to any application load balancer; assuming to
     * let port 80 and 443 pass through
     */
    SecurityGroup getDefaultSecurityGroupForApplicationLoadBalancer(Region region);

    /**
     * @return the security group that shall be assigned by default to any host used as part of the central reverse
     *         proxy cluster in a region
     */
    SecurityGroup getDefaultSecurityGroupForCentralReverseProxy(Region region);

    /**
     * @return the security group that shall be assigned by default to any host used as part of a MongoDB replica set;
     *         we expect public SSH access and local access to the default MongoDB port (usually 27017).
     */
    SecurityGroup getDefaultSecurityGroupForMongoDBHosts(Region region);
    
    /**
     * Obtains the default RabbitMQ configuration for the {@code region} specified. If nothing else is specified
     * explicitly, application server replica sets launched in the {@code region} shall use this for their replication
     * message channels and exchanges.
     */
    RabbitMQEndpoint getMessagingConfigurationForDefaultCluster(Region region);
    
    /**
     * Tells the regions supported. The underlying hyperscaler may have more, but we may not want to run in all.
     */
    Iterable<Region> getRegions();
    
    MachineImage getLatestImageWithTag(Region region, String tagName, String tagValue);

    MachineImage getLatestImageWithType(Region region, String imageType);
}
