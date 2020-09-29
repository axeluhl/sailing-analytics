package com.sap.sse.landscape.aws;

import java.util.UUID;

import com.sap.sse.common.Named;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Represents an HTTP/HTTPS reverse proxy that is able to manage redirection rule sets for "level 7" routing, primarily
 * based on hostnames but probably also on {@link Scope}s and their identifiers. If this reverse proxy employs several
 * {@link Host}s to provide its service then those are assumed to share a common configuration. This interface grants
 * access to this common configuration.
 * <p>
 * 
 * Each hostname can be mapped to at most one {@link ApplicationReplicaSet application replica set}, and only one
 * redirect strategy can be selected for such a hostname. The reverse proxy will rewrite such requests, including HTTP
 * to HTTPS forwarding and depending on the strategy selected appending a path to a bare URL with no path components
 * that point to a specific landing page.
 * <p>
 * 
 * Generally, setting a redirect for a hostname will replace an already existing mapping for an equal hostname. Clients
 * using this interface don't have to worry about the details of reloading the configuration or restarting the reverse
 * proxy service; calling the methods through this interface will do what it takes.
 * <p>
 * 
 * We think that such reverse proxies should be used as "cold-storage" handlers, mapping scopes and hostnames of content
 * through "default" load balancer rules. Such default rules should forward all requests to a target group that contains
 * the {@link #getHosts() hosts} forming this reverse proxy. By allowing for multiple hosts that all share the same
 * reverse proxy configuration, scalability as well as availability can be addressed.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ReverseProxy<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> extends Named {
    /**
     * A reverse proxy may scale out by adding more hosts.
     * 
     * @return at least one host
     */
    Iterable<AwsInstance<ShardingKey, MetricsT>> getHosts();
    
    /**
     * The target group that will be managed by this object when hosts are added and removed. This instance asserts that
     * after {@link #addHost(AwsAvailabilityZone)}, {@link #addHosts(InstanceType, AwsAvailabilityZone, int)} and
     * {@link #removeHost(AwsInstance)} the {@link TargetGroup#getRegisteredTargets()} will match the response of
     * {@link #getHosts()}.
     */
    TargetGroup<ShardingKey, MetricsT> getTargetGroup();
    
    /**
     * Adds a single host to this reverse proxy in availability zone {@code az}, using a default instance type
     * {@link InstanceType#T3_SMALL}. See {@link #addHosts(InstanceType, AwsAvailabilityZone, int)} in case you'd like
     * to specify a different instance type.
     * 
     * @return the host that was added by this request; it will also be part of the response of {@link #getHosts()} now
     */
    default AwsInstance<ShardingKey, MetricsT> addHost(AwsAvailabilityZone az) {
        return addHosts(getDefaultInstanceType(), az, /* numberOfHostsToAdd */ 1).iterator().next();
    }

    default InstanceType getDefaultInstanceType() {
        return InstanceType.T3_SMALL;
    }

    /**
     * Add zero or more hosts of the instance type specified to the availability zone {@code az}.
     * 
     * @return the hosts that were added by this request; they will also be part of the response of {@link #getHosts()}
     *         now
     */
    Iterable<AwsInstance<ShardingKey, MetricsT>> addHosts(InstanceType instanceType, AwsAvailabilityZone az, int numberOfHostsToAdd);
    
    /**
     * Configures a redirect in this reverse proxy such that requests for it will go to the
     * {@code /index.html} landing page for the application replica set provided.
     */
    void setPlainRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet);
    
    /**
     * Configures a redirect in this reverse proxy such that requests for it will go to the
     * {@code /gwt/Home.html} landing page for the application replica set provided.
     */
    void setHomeRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet);

    /**
     * Configures a redirect in this reverse proxy such that requests for it will go to the
     * event page for the event with ID {@code eventId} that is expected to be hosted by the
     * application replica set provided.
     */
    void setEventRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet, UUID eventId);

    /**
     * Configures a redirect in this reverse proxy such that requests for it will go to the event series page for the
     * event series identified by the UUID of the leaderboard group that represents the series and which is expected to
     * be hosted by the application replica set provided.
     */
    void setEventSeriesRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet,
            UUID leaderboardGroupId);
    
    /**
     * Configures a rule for requests for anything from within {@code scope} such that those requests
     * are sent to the {@code applicationReplicaSet}.
     */
    void setScopeRedirect(Scope<ShardingKey> scope, ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet);
    
    /**
     * Removes any existing redirect mapping for the {@code hostname} provided. If no such mapping
     * exists, the method does nothing.
     */
    void removeRedirect(String hostname);

    /**
     * Removes a single host from this reverse proxy. When trying to remove the last remaining host,
     * an {@link IllegalStateException} will be thrown and the method will not complete the request. Consider
     * using {@link #terminate()} to terminate all hosts forming this reverse proxy.
     */
    void removeHost(AwsInstance<ShardingKey, MetricsT> host);

    /**
     * {@link AwsLandscape#terminate(AwsInstance) Terminates} all {@link #getHosts() hosts} that form this reverse
     * proxy.
     */
    void terminate();
}
