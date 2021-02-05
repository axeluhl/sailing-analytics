package com.sap.sse.landscape.aws;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.aws.impl.Activator;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;
import com.sap.sse.landscape.aws.impl.AwsLandscapeImpl;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.impl.AwsTargetGroupImpl;
import com.sap.sse.landscape.aws.impl.SSHKeyPairListenersImpl;
import com.sap.sse.landscape.aws.impl.SSHKeyPairListenersImpl.SSHKeyPairListener;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.mongodb.MongoEndpoint;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.mongodb.MongoProcessInReplicaSet;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;
import com.sap.sse.landscape.mongodb.impl.MongoProcessImpl;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;
import com.sap.sse.landscape.ssh.SSHKeyPair;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerState;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RulePriorityPair;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.ChangeInfo;
import software.amazon.awssdk.services.route53.model.RRType;

/**
 * A simplified view onto the AWS SDK API that is geared towards specific ways and patterns of managing an application
 * and infrastructure landscape. Among others, it uses {@link Ec2Client}, {@link Route53Client},
 * {@link CloudWatchClient} and {@link ElasticLoadBalancingV2Client} to manage the underlying AWS landscape.<p>
 * 
 * An instance of this landscape interface is expected to be created by this bundle's {@link Activator} if the necessary
 * credentials have been supplied; it is registered with the OSGi service registry under this interface.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 */
public interface AwsLandscape<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends Landscape<ShardingKey, MetricsT, ProcessT> {
    String ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME = "com.sap.sse.landscape.aws.accesskeyid";

    String SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME = "com.sap.sse.landscape.aws.secretaccesskey";
    
    /**
     * The name of the tag used on {@link AwsInstance hosts} running one or more {@link MongoProcess}(es). The tag value
     * provides information about the replica sets and the ports on which the respective {@link MongoProcess} is listening.
     * If no port is explicitly specified, the default port {@link MongoProcess#DEFAULT_PORT 27017} is assumed. If the replica
     * set name is empty then the port or at least ":" must be specified and a standalone process is assumed. The
     * format for the tag value is as follows:<pre>
     *   ({replica-set-name}(:{port})?)(,({replica-set-name}(:{port})?))*
     * </pre>
     * In other words, the tag value is expected to be a comma-separated list of replica set name and optional port specifications
     * where the optional port specification is appended to the replica set name separated by a colon (:).<p>
     * 
     * Examples:<pre>
     *   archive:10201,:10202,live:10203
     *   :
     * </pre>
     * The first example means that three MongoDB processes are running on the host, one listening on port 10201 which is part of the
     * replica set "archive", one standalone instance listening on port 10202, and a member of the "live" replica set running
     * on port 10203. The second example (":") means that a single standalong MongoDB process is assumed to be running on the
     * default port (usually 27017).
     */
    String MONGO_REPLICA_SETS_TAG_NAME = "mongo-replica-sets";

    String MONGO_DEFAULT_REPLICA_SET_NAME = "live";
    
    String MONGO_REPLICA_SET_NAME_AND_PORT_SEPARATOR = ":";
    
    /**
     * Tag name used to identify instances on which a RabbitMQ installation is running. The tag value is currently interpreted to
     * be the port number (usually 5672) on which the RabbitMQ endpoint can be reached.
     */
    String RABBITMQ_TAG_NAME = "RabbitMQEndpoint";
    
    String CENTRAL_REVERSE_PROXY_TAG_NAME = "CentralReverseProxy";
    
    SSHKeyPairListeners SSH_KEY_PAIR_LISTENERS = new SSHKeyPairListenersImpl();
    
    /**
     * Listeners added here will be added to each {@link AwsLandscape} object returned by {@link #obtain()} / {@link #obtain(String, String)}
     * and hence will be notified each time a change occurs to the set of SSH key pairs.
     */
    static void addSSHKeyPairListener(SSHKeyPairListener listener) {
        SSH_KEY_PAIR_LISTENERS.addSSHKeyPairListener(listener);
    }
    
    /**
     * Listeners removed here will no longer be added to new {@link AwsLandscape} objects returned by {@link #obtain()}
     * / {@link #obtain(String, String)} and hence will no longer notified of changes to the set of SSH key pairs for new
     * {@link AwsLandscape} objects.
     */
    static void removeSSHKeyPairListener(SSHKeyPairListener listener) {
        SSH_KEY_PAIR_LISTENERS.removeSSHKeyPairListener(listener);
    }
    
    /**
     * Based on system properties for the AWS access key ID and the secret access key (see
     * {@link #ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME} and {@link #SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME}), this method
     * returns a landscape object which internally has access to the clients for the underlying AWS landscape, such as
     * an EC2 client, a Route53 client, etc.
     */
    static <ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    AwsLandscape<ShardingKey, MetricsT, ProcessT> obtain() {
        final AwsLandscape<ShardingKey, MetricsT, ProcessT> result = new AwsLandscapeImpl<>();
        result.addSSHKeyPairListeners(SSH_KEY_PAIR_LISTENERS.getSshKeyPairListeners());
        return result;
    }
    
    /**
     * Based on an explicit AWS access key ID and the secret access key, this method returns a landscape object which
     * internally has access to the clients for the underlying AWS landscape, such as an EC2 client, a Route53 client,
     * etc.
     */
    static <ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    AwsLandscape<ShardingKey, MetricsT, ProcessT> obtain(String accessKey, String secret) {
        final AwsLandscape<ShardingKey, MetricsT, ProcessT> result = new AwsLandscapeImpl<>(accessKey, secret);
        result.addSSHKeyPairListeners(SSH_KEY_PAIR_LISTENERS.getSshKeyPairListeners());
        return result;
    }
    
    void addSSHKeyPairListeners(Iterable<SSHKeyPairListener> sshKeyPairListeners);

    default AwsInstance<ShardingKey, MetricsT> launchHost(MachineImage image, InstanceType instanceType,
            AwsAvailabilityZone availabilityZone, String keyName, Iterable<SecurityGroup> securityGroups,
            Optional<Tags> tags, String... userData) {
        final HostSupplier<ShardingKey, MetricsT, ProcessT, AwsInstance<ShardingKey, MetricsT>> hostSupplier =
                (instanceId, az, landscape)->new AwsInstanceImpl<ShardingKey, MetricsT>(instanceId, az, landscape);
        return launchHost(hostSupplier, image, instanceType, availabilityZone, keyName, securityGroups, tags, userData);
    }
    
    /**
     * Launches a new {@link Host} from a given image into the availability zone specified and controls network access
     * to that instance by setting the security groups specified for the resulting host.
     * @param keyName
     *            the SSH key pair name to use when launching; this will grant root access with the corresponding
     *            private key; see also {@link #getKeyPairInfo(Region, String)}
     * @param userData
     *            zero or more strings representing the user data to be passed to the instance; multiple strings will be
     *            concatenated, using the line separator to join them. The instance is able to read the user data throuh
     *            the AWS SDK installed on the instance.
     */
    default <HostT extends AwsInstance<ShardingKey, MetricsT>> HostT launchHost(
            HostSupplier<ShardingKey, MetricsT, ProcessT, HostT> hostSupplier,
            MachineImage fromImage, InstanceType instanceType, AwsAvailabilityZone az, String keyName,
            Iterable<SecurityGroup> securityGroups, Optional<Tags> tags, String... userData) {
        return launchHosts(hostSupplier, /* numberOfHostsToLaunch */ 1, fromImage, instanceType, az, keyName,
                securityGroups, tags, userData).iterator().next();
    }

    // -------------------- technical landscape services -----------------
    
    /**
     * Launches a number of new {@link Host}s from a given image into the availability zone specified and controls
     * network access to that instance by setting the security groups specified for the resulting host.
     * @param keyName
     *            the SSH key pair name to use when launching; this will grant root access with the corresponding
     *            private key; see also {@link #getKeyPairInfo(Region, String)}
     */
    <HostT extends AwsInstance<ShardingKey, MetricsT>> Iterable<HostT> launchHosts(
            HostSupplier<ShardingKey, MetricsT, ProcessT, HostT> hostSupplier, int numberOfHostsToLaunch,
            MachineImage fromImage, InstanceType instanceType,
            AwsAvailabilityZone az, String keyName, Iterable<SecurityGroup> securityGroups, Optional<Tags> tags,
            String... userData);

    AmazonMachineImage<ShardingKey, MetricsT> getImage(Region region, String imageId);

    AmazonMachineImage<ShardingKey, MetricsT> createImage(AwsInstance<ShardingKey, MetricsT> instance, String imageName, Optional<Tags> tags);

    void deleteImage(Region region, String imageId);

    AmazonMachineImage<ShardingKey, MetricsT> getLatestImageWithTag(Region region, String tagName, String tagValue);
    
    default AmazonMachineImage<ShardingKey, MetricsT> getLatestImageWithType(Region region, String imageType) {
        return getLatestImageWithTag(region, IMAGE_TYPE_TAG_NAME, imageType);
    }

    Iterable<String> getMachineImageTypes(Region region);
    
    void setSnapshotName(Region region, String snapshotId, String snapshotName);
    
    void deleteSnapshot(Region region, String snapshotId);
    
    /**
     * Finds EC2 instances in the {@code region} that have a tag named {@code tagName} with value {@code tagValue}. The
     * result includes instances regardless their state; they are not required to be RUNNING.
     * 
     * @see #getRunningHostsWithTagValue(Region, String)
     */
    Iterable<AwsInstance<ShardingKey, MetricsT>> getHostsWithTagValue(Region region, String tagName, String tagValue);

    /**
     * Finds EC2 instances in the {@code region} that have a tag named {@code tagName}. The tag may have any value. The
     * result includes instances regardless their state; they are not required to be RUNNING.
     * 
     * @see #getRunningHostsWithTag(Region, String)
     */
    Iterable<AwsInstance<ShardingKey, MetricsT>> getHostsWithTag(Region region, String tagName);
    
    /**
     * Finds EC2 instances in the {@code region} that have a tag named {@code tagName}. The tag may have any value. The
     * instances returned have been in state RUNNING at the time of the request.
     */
    Iterable<AwsInstance<ShardingKey, MetricsT>> getRunningHostsWithTag(Region region, String tagName);

    /**
     * Finds EC2 instances in the {@code region} that have a tag named {@code tagName} with value {@code tagValue}. The
     * instances returned have been in state RUNNING at the time of the request.
     */
    Iterable<AwsInstance<ShardingKey, MetricsT>> getRunningHostsWithTagValue(Region region, String tagName,
            String tagValue);

    KeyPairInfo getKeyPairInfo(Region region, String keyName);

    Iterable<KeyPairInfo> getAllKeyPairInfos(Region region);
    
    void deleteKeyPair(Region region, String keyName);
    
    /**
     * Uploads the public key to AWS under the name "keyName", stores it in this landscape and returns the key pair.<p>
     * 
     * The calling subject must have {@code CREATE} permission for the key and the {@code CREATE_OBJECT} permission for
     * the current server.
     */
    SSHKeyPair importKeyPair(Region region, byte[] publicKey, byte[] encryptedPrivateKey, String keyName) throws JSchException;

    void terminate(AwsInstance<ShardingKey, MetricsT> host);

    /**
     * The calling subject must have {@code READ} permission for the key requested.
     */
    SSHKeyPair getSSHKeyPair(Region region, String keyName);
    
    /**
     * Obtains all SSH key pairs known by this landscape. Clients shall check the {@code READ} permission before handing out those keys.
     */
    Iterable<SSHKeyPair> getSSHKeyPairs();

    /**
     * Assumes that the {@code keyPair}'s private key has been encrypted using this landscape's default encryption passphrase
     * and uses that to decrypt it.
     */
    byte[] getDecryptedPrivateKey(SSHKeyPair keyPair, byte[] privateKeyEncryptionPassphrase) throws JSchException;

    /**
     * Adds a key pair with {@link KeyPair#decrypt(byte[]) decrypted} private key to the AWS {@code region} identified
     * and stores it persistently also in the local server's database with the private key encrypted.
     * <p>
     * 
     * The calling subject must have {@code CREATE} permission for the key and the {@code CREATE_OBJECT} permission for
     * the current server.
     */
    SSHKeyPair addSSHKeyPair(com.sap.sse.landscape.Region region, String creator, String keyName, KeyPair keyPairWithDecryptedPrivateKey) throws JSchException;

    /**
     * Creates a key pair with the given name in the region specified and obtains the key details and stores them in
     * this landscape persistently, such that {@link #getKeyPairInfo(Region, String)} as well as
     * {@link #getSSHKeyPair(Region, String)} will be able to obtain (information on) the key. The private key is
     * stored encrypted with the passphrase provided as parameter {@code privateKeyEncryptionPassphrase}.<p>
     * 
     * The calling subject must have {@code CREATE} permission for the key and the {@code CREATE_OBJECT} permission for
     * the current server.
     * 
     * @return the key ID as string, usually starting with the prefix "key-"
     */
    SSHKeyPair createKeyPair(Region region, String keyName, byte[] privateKeyEncryptionPassphrase) throws JSchException;

    Instance getInstance(String instanceId, Region region);

    /**
     * @param hostname the fully-qualified host name
     */
    ChangeInfo setDNSRecordToHost(String hostedZoneId, String hostname, Host host);

    /**
     * Creates a {@code CNAME} record in the DNS's hosted zone identified by the {@code hostedZoneId} that lets
     * {@code hostname} point to the {@code alb} application load balancer's {@link ApplicationLoadBalancer#getDNSName()
     * A record name}.
     * 
     * @param hostname
     *            the fully-qualified host name
     */
    ChangeInfo setDNSRecordToApplicationLoadBalancer(String hostedZoneId, String hostname, ApplicationLoadBalancer<ShardingKey, MetricsT> alb);

    String getDNSHostedZoneId(String hostedZoneName);

    /**
     * @param hostname the fully-qualified host name
     */
    ChangeInfo setDNSRecordToValue(String hostedZoneId, String hostname, String value);

    /**
     * @param hostname
     *            the fully-qualified host name
     * @param value
     *            the address to which the record to remove did resolve the hostname, e.g., the value passed to the
     *            {@link #setDNSRecordToValue(String, String, String)} earlier
     */
    ChangeInfo removeDNSRecord(String hostedZoneId, String hostname, RRType type, String value);

    /**
     * Removes the A record (IPv4 address) for {@code hostname}
     * 
     * @param hostname
     *            the fully-qualified host name
     * @param value
     *            the address to which the record to remove did resolve the hostname, e.g., the value passed to the
     *            {@link #setDNSRecordToValue(String, String, String)} earlier
     */
    ChangeInfo removeDNSRecord(String hostedZoneId, String hostname, String value);
    
    ChangeInfo getUpdatedChangeInfo(ChangeInfo changeInfo);

    Iterable<ApplicationLoadBalancer<ShardingKey, MetricsT>> getLoadBalancers(Region region);
    
    ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancer(String loadBalancerArn, Region region);

    ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancerByName(String name, Region region);

    /**
     * Creates an application load balancer with the name and in the region specified. The method returns once the request
     * has been responded to. The load balancer may still be in a pre-ready state. Use {@link #getApplicationLoadBalancerStatus(ApplicationLoadBalancer)}
     * to find out more.
     */
    ApplicationLoadBalancer<ShardingKey, MetricsT> createLoadBalancer(String name, Region region);

    Iterable<Listener> getListeners(ApplicationLoadBalancer<ShardingKey, MetricsT> alb);
    
    LoadBalancerState getApplicationLoadBalancerStatus(ApplicationLoadBalancer<ShardingKey, MetricsT> alb);

    Iterable<AvailabilityZone> getAvailabilityZones(Region awsRegion);

    AwsAvailabilityZone getAvailabilityZoneByName(Region region, String availabilityZoneName);

    /**
     * Deletes this load balancer and all its target groups (the target groups to which this load balancer currently
     * forwards any traffic).
     */
    void deleteLoadBalancer(ApplicationLoadBalancer<ShardingKey, MetricsT> alb);
    
    /**
     * All target groups that have the load balancer identified by the ARN as "their" load balancer which means that
     * this load balancer is forwarding traffic to all those target groups.
     */
    Iterable<TargetGroup<ShardingKey, MetricsT>> getTargetGroupsByLoadBalancerArn(Region region, String loadBalancerArn);

    /**
     * Looks up a target group by its name in a region. The main reason is to obtain the target group's ARN in order to
     * enable construction of the {@link TargetGroup} wrapper object.
     * 
     * @return {@code null} if no target group named according to the value of {@code targetGroupName} is found in the
     *         {@code region}
     */
    TargetGroup<ShardingKey, MetricsT> getTargetGroup(Region region, String targetGroupName);

    /**
     * Creates a target group with a default configuration that includes a health check URL. Stickiness is enabled with
     * the default duration of one day. The load balancing algorithm is set to {@code least_outstanding_requests}.
     * The protocol (HTTP or HTTPS) is inferred from the port: 443 means HTTPS; anything else means HTTP.
     */
    TargetGroup<ShardingKey, MetricsT> createTargetGroup(Region region, String targetGroupName, int port,
            String healthCheckPath, int healthCheckPort);

    default TargetGroup<ShardingKey, MetricsT> getTargetGroup(Region region, String targetGroupName, String targetGroupArn) {
        return new AwsTargetGroupImpl<>(this, region, targetGroupName, targetGroupArn);
    }

    software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup getAwsTargetGroup(Region region, String targetGroupName);

    software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup getAwsTargetGroupByArn(Region region, String targetGroupArn);

    Map<AwsInstance<ShardingKey, MetricsT>, TargetHealth> getTargetHealthDescriptions(TargetGroup<ShardingKey, MetricsT> targetGroup);

    <SK, MT extends ApplicationProcessMetrics> void deleteTargetGroup(TargetGroup<SK, MT> targetGroup);

    Iterable<Rule> getLoadBalancerListenerRules(Listener loadBalancerListener, Region region);

    /**
     * Use {@link Rule.Builder} to create {@link Rule} objects you'd like to set for the {@link Listener} passed as parameter.
     * Obviously, there is no need to set the {@link Rule.Builder#ruleArn(String) rule's ARN} as this is created by executing
     * the request.
     * 
     * @return the rule objects, now including the {@link Rule#ruleArn() rule ARNs}, created by this request
     */
    Iterable<Rule> createLoadBalancerListenerRules(Region region, Listener listener, Rule... rulesToAdd);

    void deleteLoadBalancerListenerRules(Region region, Rule... rulesToDelete);

    void updateLoadBalancerListenerRulePriorities(Region region, Collection<RulePriorityPair> newRulePriorities);

    void deleteLoadBalancerListener(Region region, Listener listener);

    SecurityGroup getSecurityGroup(String securityGroupId, Region region);

    void addTargetsToTargetGroup(
            TargetGroup<ShardingKey, MetricsT> targetGroup,
            Iterable<AwsInstance<ShardingKey, MetricsT>> targets);

    void removeTargetsFromTargetGroup(
            TargetGroup<ShardingKey, MetricsT> targetGroup,
            Iterable<AwsInstance<ShardingKey, MetricsT>> targets);

    LoadBalancer getAwsLoadBalancer(String loadBalancerArn, Region region);
    
    // --------------- abstract landscape view --------------
    /**
     * Obtains the reverse proxy in the given {@code region} that is used to receive (and possibly redirect to HTTPS or
     * forward to a host proxied by the reverse proxy) all HTTP requests and any HTTPS request not handled by a
     * dedicated load balancer rule, such as "cold storage" hostnames that have been archived. May return {@code null}
     * in case in the given {@code region} no such reverse proxy has been configured / set up yet.
     */
    ReverseProxyCluster<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> getCentralReverseProxy(Region region);
    
    /**
     * Each region can have a single load balancer per {@code wildcardDomain} that is the target for any sub-domain of
     * that wildcard domain. For example, there is a dynamic load balancer for "sapsailing.com" that is the default for
     * any sub-domain that does not have a dedicated DNS entry (usually then pointing at a dedicated
     * {@link #getDNSMappedLoadBalancerFor(Region, String) DNS-mapped load balancer}). The wildcard domain is used
     * in the construction of the load balancer's name which has to be unique per region.<p>
     * 
     * This load balancer shall be used for rather short-lived scope mappings and their rules because changes become
     * effective immediately with the change, other than for DNS records, for example, which take a while to propagate
     * through the world-wide DNS infrastructure.
     * 
     * @param wildcardDomain e.g., "sapsailing.com" without leading and without trailing dot
     * 
     */
    ApplicationLoadBalancer<ShardingKey, MetricsT> getNonDNSMappedLoadBalancer(Region region, String wildcardDomain);
    
    /**
     * Creates an application load balancer (ALB) intended to serve requests for dynamically-mapped sub-domains where
     * only a wildcard DNS record exists for the domain. There is a naming rule in place such that
     * {@link #getNonDNSMappedLoadBalancer(Region, String)}, when called with an equal {@code wildcardDomain} and
     * {@code region}, will deliver the load balancer created by this call.
     */
    ApplicationLoadBalancer<ShardingKey, MetricsT> createNonDNSMappedLoadBalancer(Region region, String wildcardDomain);
    
    /**
     * Looks up the hostname in the DNS and assumes to get a load balancer CNAME record for it that exists in the {@code region}
     * specified. The load balancer is then looked up by its {@link ApplicationLoadBalancer#getDNSName() host name}.
     */
    ApplicationLoadBalancer<ShardingKey, MetricsT> getDNSMappedLoadBalancerFor(Region region, String hostname);
    
    /**
     * The default MongoDB configuration to connect to. See also {@link #MONGO_DEFAULT_REPLICA_SET_NAME} and
     * {@link #getDatabaseConfigurationForReplicaSet(Region, String)}, but expect that this could also be a
     * standalone MongoDB instance.
     */
    MongoEndpoint getDatabaseConfigurationForDefaultReplicaSet(Region region);
    
    /**
     * Computes the {@link Tags tag value} to add for the {@link #MONGO_REPLICA_SETS_TAG_NAME} tag to represent the
     * configuration of the {@code mongoProcess}. In line with
     * {@link #getDatabaseConfigurationForReplicaSet(Region, String)}, this will encode the replica set name, if any,
     * followed by a colon and the port number into the tag value.
     * 
     * @param tagsToAddTo
     *            must not be {@code null}; the {@link #MONGO_REPLICA_SETS_TAG_NAME} tag will be
     *            {@link Tags#and(String, String) added}.
     * @return the {@code tagsToAddTo} object, for chaining
     */
    Tags getTagForMongoProcess(Tags tagsToAddTo, String replicaSetName, int port);

    /**
     * Searches the region for {@link AwsInstance hosts} that are tagged with the {@link #MONGO_REPLICA_SETS_TAG_NAME}
     * tag such that the {@code mongoReplicaSetName} is part of the host's specification. For each such host found, a
     * {@link MongoProcessInReplicaSet} is constructed and added to the {@link MongoReplicaSet} returned from which the
     * called may obtain the {@link MongoReplicaSet#getInstances() replicas} and from them, e.g., the
     * {@link MongoProcess#getPort() port} and the host name
     * ({@link MongoProcess#getHost()}.{@link AwsInstance#getPrivateAddress() getPrivateAddress()}).
     * 
     * @param mongoReplicaSetName
     *            must not be {@code null}. If you're looking for a standalone {@link MongoProcess}, simply use the
     *            {@link MongoProcessImpl} constructor.
     */
    MongoReplicaSet getDatabaseConfigurationForReplicaSet(com.sap.sse.landscape.Region region, String mongoReplicaSetName);

    Iterable<MongoEndpoint> getMongoEndpoints(Region region);

    /**
     * Gets a default RabbitMQ configuration for the {@code region} specified.<p>
     * 
     * TODO For now, the method searches for accordingly-tagged instances and picks the first one it finds. We need to extend this to RabbitMQ replication.
     */
    RabbitMQEndpoint getDefaultRabbitConfiguration(AwsRegion region);

    Database getDatabase(Region region, String databaseName);

    /**
     * The region to use as the default region for instance creation, DB connectivity, reverse proxy config, ...
     */
    AwsRegion getDefaultRegion();

    /**
     * Looks for a tag with key as specified by {@code tagName} on the {@code host} specified. If found, the tag's value
     * is returned. Otherwise, the {@link Optional} returned {@link Optional#isPresent() is not present}.
     */
    Optional<String> getTag(AwsInstance<ShardingKey, MetricsT> host, String tagName);

    /**
     * Obtains all hosts with a tag named {@code tagName}, regardless the tag's value, and returns them as
     * {@link ApplicationProcessHost}s. Callers have to provide the bi-function that produces instances of the desired
     * {@link ApplicationProcess} subtype for each server directory holding a process installation on that host.
     * 
     * @param processFactoryFromHostAndServerDirectory
     *            takes the host and the server directory as arguments and is expected to produce an
     *            {@link ApplicationProcess} object of some sort.
     */
    Iterable<ApplicationProcessHost<ShardingKey, MetricsT, ProcessT>> getApplicationProcessHostsByTag(Region region,
            String tagName, BiFunction<Host, String, ProcessT> processFactoryFromHostAndServerDirectory);

    /**
     * Obtains all {@link #getApplicationProcessHostsByTag(Region, String, BiFunction) hosts} with a tag whose key is
     * specified by {@code tagName} and discovers all application server processes configured on it. These are then
     * grouped by {@link ApplicationProcess#getServerName(Optional, byte[]) server name}, and using
     * {@link ApplicationProcess#getMasterServerName(Optional)} the master/replica relationships between the processes with equal server
     * name are discovered. From this, an {@link ApplicationReplicaSet} is established per server name.
     * @param processFactoryFromHostAndServerDirectory
     *            takes the host and the server directory as arguments and is expected to produce an
     *            {@link ApplicationProcess} object of some sort.
     * @param optionalTimeout
     *            an optional timeout for communicating with the application server(s) to try to read the application
     *            configuration; used, e.g., as timeout during establishing SSH connections
     */
    Iterable<ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT>> getApplicationReplicaSetsByTag(Region region,
            String tagName, BiFunction<Host, String, ProcessT> processFactoryFromHostAndServerDirectory,
            Optional<Duration> optionalTimeout, byte[] privateKeyEncryptionPassphrase) throws Exception;

}
