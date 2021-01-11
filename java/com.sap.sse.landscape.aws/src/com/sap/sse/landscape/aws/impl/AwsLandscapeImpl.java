package com.sap.sse.landscape.aws.impl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.application.impl.ApplicationReplicaSetImpl;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.ReverseProxyCluster;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.persistence.DomainObjectFactory;
import com.sap.sse.landscape.aws.persistence.MongoObjectFactory;
import com.sap.sse.landscape.aws.persistence.PersistenceFactory;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.mongodb.MongoEndpoint;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;
import com.sap.sse.landscape.mongodb.impl.DatabaseImpl;
import com.sap.sse.landscape.mongodb.impl.MongoProcessImpl;
import com.sap.sse.landscape.mongodb.impl.MongoProcessInReplicaSetImpl;
import com.sap.sse.landscape.mongodb.impl.MongoReplicaSetImpl;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.SessionUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.DescribeAvailabilityZonesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeTagsResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.ImportKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.ec2.model.Placement;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.ResourceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest.Builder;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagSpecification;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Certificate;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateTargetGroupRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteTargetGroupRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetGroupsRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetGroupsResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerAttribute;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerNotFoundException;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerState;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ModifyTargetGroupAttributesRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RulePriorityPair;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.SetRulePrioritiesRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.SubnetMapping;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupAttribute;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetTypeEnum;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ChangeBatch;
import software.amazon.awssdk.services.route53.model.ChangeInfo;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.GetChangeRequest;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

public class AwsLandscapeImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
implements AwsLandscape<ShardingKey, MetricsT, ProcessT> {
    private static final String DEFAULT_TARGET_GROUP_PREFIX = "D";
    private static final Logger logger = Logger.getLogger(AwsLandscapeImpl.class.getName());
    private static final long DEFAULT_DNS_TTL_MILLIS = 60000l;
    // TODO <config> the sapsailing.com certificate's ARN where the certificate is valid until 2021-05-07; we need a certifiate per region
    private static final String DEFAULT_CERTIFICATE_ARN = "arn:aws:acm:eu-west-2:017363970217:certificate/48c51d6a-b4f2-4fa0-8dcc-426cd9c7aadc";
    // TODO <config> the "Java Application with Reverse Proxy" security group in eu-west-2 for experimenting; we need this security group per region
    private static final String DEFAULT_APPLICATION_SERVER_SECURITY_GROUP_ID_EU_WEST_1 = "sg-eaf31e85";
    private static final String DEFAULT_APPLICATION_SERVER_SECURITY_GROUP_ID_EU_WEST_2 = "sg-0b2afd48960251280";
    private static final String DEFAULT_MONGODB_SECURITY_GROUP_ID_EU_WEST_1 = "sg-0a9bc2fb61f10a342";
    private static final String DEFAULT_MONGODB_SECURITY_GROUP_ID_EU_WEST_2 = "sg-02649c35a73ee0ae5";
    private static final String DEFAULT_NON_DNS_MAPPED_ALB_NAME = "DefDyn";
    private final String accessKeyId;
    private final String secretAccessKey;
    private final MongoObjectFactory mongoObjectFactory;
    private ConcurrentMap<Pair<String, String>, SSHKeyPair> sshKeyPairs;
    private final AwsRegion globalRegion;
    private final String s3BucketForAlbLogs; // TODO this will have to be a bucket-per-Region map eventually...
    
    /**
     * Used for the symmetric encryption / decryption of private SSH keys. See also
     * {@link #getDecryptedPrivateKey(SSHKeyPair)}.
     */
    private final byte[] privateKeyEncryptionPassphrase;
    
    public AwsLandscapeImpl() {
        this(System.getProperty(ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME), System.getProperty(SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME),
                // by using MongoDBService.INSTANCE the default test configuration will be used if nothing else is configured
                PersistenceFactory.INSTANCE.getDomainObjectFactory(MongoDBService.INSTANCE),
                PersistenceFactory.INSTANCE.getMongoObjectFactory(MongoDBService.INSTANCE), System.getProperty(S3_BUCKET_FOR_ALB_LOGS_SYSTEM_PROPERTY_NAME));
    }
    
    public AwsLandscapeImpl(String accessKeyId, String secretAccessKey,
            DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory, String s3BucketForAlbLogs) {
        this.privateKeyEncryptionPassphrase = ("aw4raif87l"+"098sf;;50").getBytes();
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.globalRegion = new AwsRegion(Region.AWS_GLOBAL);
        this.mongoObjectFactory = mongoObjectFactory;
        this.sshKeyPairs = new ConcurrentHashMap<Util.Pair<String,String>, SSHKeyPair>();
        this.s3BucketForAlbLogs = s3BucketForAlbLogs;
        for (final SSHKeyPair keyPair : domainObjectFactory.loadSSHKeyPairs()) {
            internalAddKeyPair(keyPair);
        }
    }
    
    /**
     * No persistence, no replication.
     */
    private void internalAddKeyPair(SSHKeyPair keyPair) {
        sshKeyPairs.put(new Pair<>(keyPair.getRegionId(), keyPair.getName()), keyPair);
    }
    
    
    private static byte[] getPrivateKeyBytes(KeyPair unencryptedKeyPair) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        unencryptedKeyPair.writePrivateKey(bos);
        return bos.toByteArray();
    }

    @Override
    public void addSSHKeyPair(com.sap.sse.landscape.Region region, String creator, String keyName, KeyPair keyPairWithDecryptedPrivateKey) {
        addSSHKeyPair(new SSHKeyPair(region.getId(), creator, TimePoint.now(), keyName, keyPairWithDecryptedPrivateKey.getPublicKeyBlob(),
                getPrivateKeyBytes(keyPairWithDecryptedPrivateKey)));
    }
    
    private void addSSHKeyPair(SSHKeyPair keyPair) {
        internalAddKeyPair(keyPair);
        mongoObjectFactory.storeSSHKeyPair(keyPair);
    }
    
    @Override
    public SSHKeyPair createKeyPair(com.sap.sse.landscape.Region region, String keyName) throws JSchException {
        final CreateKeyPairResponse keyPairResponse = getEc2Client(getRegion(region))
                .createKeyPair(CreateKeyPairRequest.builder().keyName(keyName).build());
        final String keyMaterial = keyPairResponse.keyMaterial();
        Object principal;
        try {
            principal = SessionUtils.getPrincipal();
        } catch (Exception e) {
            logger.severe("Problem determining current user: "+e.getMessage());
            principal = null;
        }
        final SSHKeyPair result = new SSHKeyPair(region.getId(), principal==null?"":principal.toString(),
                TimePoint.now(), keyPairResponse.keyName(), /* public key not known */ null, keyMaterial.getBytes(),
                privateKeyEncryptionPassphrase);
        addSSHKeyPair(result);
        return result;
    }

    private <B extends AwsClientBuilder<B, C>, C> C getClient(B clientBuilder, Region region) {
        return clientBuilder.credentialsProvider(this::getCredentials).region(region).build();
    }
    
    private Ec2Client getEc2Client(Region region) {
        return getClient(Ec2Client.builder(), region);
    }
    
    private ElasticLoadBalancingV2Client getLoadBalancingClient(Region region) {
        return getClient(ElasticLoadBalancingV2Client.builder(), region);
    }
    
    @Override
    public ApplicationLoadBalancer<ShardingKey, MetricsT> createLoadBalancer(String name, com.sap.sse.landscape.Region region) {
        Region awsRegion = getRegion(region);
        final ElasticLoadBalancingV2Client client = getLoadBalancingClient(awsRegion);
        final Iterable<AvailabilityZone> availabilityZones = getAvailabilityZones(region);
        final SubnetMapping[] subnetMappings = Util.toArray(Util.map(getSubnetsForAvailabilityZones(awsRegion, availabilityZones),
                subnet->SubnetMapping.builder().subnetId(subnet.subnetId()).build()), new SubnetMapping[0]);
        final CreateLoadBalancerResponse response = client
                .createLoadBalancer(CreateLoadBalancerRequest.builder().name(name)
                        .subnetMappings(subnetMappings).build());
        client.modifyLoadBalancerAttributes(b->b.loadBalancerArn(response.loadBalancers().iterator().next().loadBalancerArn()).
                attributes(
                        LoadBalancerAttribute.builder().key("access_logs.s3.enabled").value("true").build(),
                        LoadBalancerAttribute.builder().key("access_logs.s3.bucket").value(s3BucketForAlbLogs).build(),
                        LoadBalancerAttribute.builder().key("idle_timeout.timeout_seconds").value("4000").build()).build());
        final ApplicationLoadBalancer<ShardingKey, MetricsT> result = new ApplicationLoadBalancerImpl<>(region, response.loadBalancers().iterator().next(), this);
        createLoadBalancerListener(result, ProtocolEnum.HTTP);
        createLoadBalancerListener(result, ProtocolEnum.HTTPS);
        return result;
    }
    
    private Listener createLoadBalancerListener(ApplicationLoadBalancer<ShardingKey, MetricsT> alb, ProtocolEnum protocol) {
        final int port = protocol==ProtocolEnum.HTTP?80:443;
        final ReverseProxyCluster<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> reverseProxy = getCentralReverseProxy(alb.getRegion());
        final TargetGroup<ShardingKey, MetricsT> defaultTargetGroup = createTargetGroup(alb.getRegion(), DEFAULT_TARGET_GROUP_PREFIX+alb.getName()+"-"+protocol.name(),
                port, reverseProxy.getHealthCheckPath(), /* healthCheckPort */ port);
        defaultTargetGroup.addTargets(reverseProxy.getHosts());
        return getLoadBalancingClient(
                getRegion(alb.getRegion()))
                        .createListener(l -> {
                            l.loadBalancerArn(alb.getArn()).protocol(protocol)
                                        .port(port)
                                        .defaultActions(Action.builder()
                                                .targetGroupArn(defaultTargetGroup.getTargetGroupArn())
                                                .type(ActionTypeEnum.FORWARD)
                                                .forwardConfig(f -> f.targetGroups(TargetGroupTuple.builder()
                                                        .targetGroupArn(defaultTargetGroup.getTargetGroupArn()).build())
                                                        .build())
                                                .build());
                            if (protocol==ProtocolEnum.HTTPS) {
                                l.certificates(Certificate.builder().certificateArn(DEFAULT_CERTIFICATE_ARN).build());
                            }
                        })
                        .listeners().iterator().next();
    }

    @Override
    public void deleteLoadBalancer(ApplicationLoadBalancer<ShardingKey, MetricsT> alb) {
        getLoadBalancingClient(getRegion(alb.getRegion())).deleteLoadBalancer(DeleteLoadBalancerRequest.builder().loadBalancerArn(alb.getArn()).build());
    }

    @Override
    public Iterable<TargetGroup<ShardingKey, MetricsT>> getTargetGroupsByLoadBalancerArn(com.sap.sse.landscape.Region region, String loadBalancerArn) {
        return Util.map(getLoadBalancingClient(getRegion(region)).describeTargetGroups(tg->tg.loadBalancerArn(loadBalancerArn)).targetGroups(),
                tg->new AwsTargetGroupImpl<>(this, region, tg.targetGroupName(), tg.targetGroupArn()));
    }

    @Override
    public Iterable<Listener> getListeners(ApplicationLoadBalancer<ShardingKey, MetricsT> alb) {
        final ElasticLoadBalancingV2Client client = getLoadBalancingClient(getRegion(alb.getRegion()));
        return client.describeListeners(b->b.loadBalancerArn(alb.getArn())).listeners();
    }

    @Override
    public LoadBalancerState getApplicationLoadBalancerStatus(ApplicationLoadBalancer<ShardingKey, MetricsT> alb) {
        final ElasticLoadBalancingV2Client client = getLoadBalancingClient(getRegion(alb.getRegion()));
        final DescribeLoadBalancersResponse response = client.describeLoadBalancers(b->b.loadBalancerArns(alb.getArn()));
        return response.loadBalancers().iterator().next().state();
    }

    @Override
    public Iterable<Rule> getLoadBalancerListenerRules(Listener loadBalancerListener, com.sap.sse.landscape.Region region) {
        return getLoadBalancingClient(getRegion(region)).describeRules(b->b.listenerArn(loadBalancerListener.listenerArn())).rules();
    }

    @Override
    public Iterable<Rule> createLoadBalancerListenerRules(com.sap.sse.landscape.Region region,
            Listener loadBalancerListenerToAddRuleTo, Rule... rulesToAdd) {
        final List<Rule> result = new ArrayList<>();
        for (final Rule rule : rulesToAdd) {
            result.add(getLoadBalancingClient(getRegion(region)).createRule(b -> b
                    .listenerArn(loadBalancerListenerToAddRuleTo.listenerArn())
                    .conditions(rule.conditions())
                    .priority(Integer.valueOf(rule.priority()))
                    .actions(rule.actions())).rules().iterator().next());
        }
        return result;
    }
    
    @Override
    public void deleteLoadBalancerListenerRules(com.sap.sse.landscape.Region region, Rule... rulesToDelete) {
        for (final Rule rule : rulesToDelete) {
            getLoadBalancingClient(getRegion(region)).deleteRule(b -> b.ruleArn(rule.ruleArn()));
        }
    }
    
    @Override
    public void updateLoadBalancerListenerRulePriorities(com.sap.sse.landscape.Region region, Collection<RulePriorityPair> newRulePriorities) {
        getLoadBalancingClient(getRegion(region)).setRulePriorities(SetRulePrioritiesRequest.builder().rulePriorities(newRulePriorities).build());
    }
    
    @Override
    public void deleteLoadBalancerListener(com.sap.sse.landscape.Region region, Listener listener) {
        getLoadBalancingClient(getRegion(region)).deleteListener(b->b.listenerArn(listener.listenerArn()));
    }

    /**
     * Grabs all subnets that are default subnet for any of the availability zones specified
     */
    private Iterable<Subnet> getSubnetsForAvailabilityZones(Region region, Iterable<AvailabilityZone> azs) {
        return Util.filter(getEc2Client(region).describeSubnets().subnets(), subnet -> subnet.defaultForAz()
                && Util.contains(Util.map(azs, az -> az.getId()), subnet.availabilityZoneId()));
    }

    @Override
    public AwsAvailabilityZone getAvailabilityZoneByName(com.sap.sse.landscape.Region region, String availabilityZoneName) {
        final software.amazon.awssdk.services.ec2.model.AvailabilityZone awsAz = getEc2Client(getRegion(region))
                .describeAvailabilityZones(
                        DescribeAvailabilityZonesRequest.builder().zoneNames(availabilityZoneName).build())
                .availabilityZones().iterator().next();
        return new AwsAvailabilityZoneImpl(awsAz);
    }
    
    @Override
    public Iterable<ApplicationLoadBalancer<ShardingKey, MetricsT>> getLoadBalancers(com.sap.sse.landscape.Region region) {
        final List<LoadBalancer> loadBalancers = getLoadBalancingClient(getRegion(region))
                .describeLoadBalancers(DescribeLoadBalancersRequest.builder().build())
                .loadBalancers();
        return Util.map(loadBalancers, lb->new ApplicationLoadBalancerImpl<>(region, lb, this));
        
    }

    @Override
    public ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancerByName(String name, com.sap.sse.landscape.Region region) {
        try {
            final DescribeLoadBalancersResponse response = getLoadBalancingClient(getRegion(region)).describeLoadBalancers(b->b.names(name));
            return response.hasLoadBalancers() ? new ApplicationLoadBalancerImpl<>(region, response.loadBalancers().iterator().next(), this) : null;
        } catch (LoadBalancerNotFoundException e) {
            return null;
        }
    }
    
    @Override
    public ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancer(String loadBalancerArn, com.sap.sse.landscape.Region region) {
        final LoadBalancer loadBalancer = getLoadBalancingClient(getRegion(region))
                .describeLoadBalancers(DescribeLoadBalancersRequest.builder().loadBalancerArns(loadBalancerArn).build())
                .loadBalancers().iterator().next();
        return new ApplicationLoadBalancerImpl<>(region, loadBalancer, this);
    }
    
    @Override
    public Instance getInstance(String instanceId, com.sap.sse.landscape.Region region) {
        return getEc2Client(getRegion(region))
                .describeInstances(DescribeInstancesRequest.builder().instanceIds(instanceId).build()).reservations()
                .iterator().next().instances().iterator().next();
    }

    private Route53Client getRoute53Client() {
        return Route53Client.builder().region(getRegion(globalRegion)).build();
    }
    
    @Override
    public ChangeInfo setDNSRecordToHost(String hostedZoneId, String hostname, Host host) {
        final String ipAddressAsString = host.getPublicAddress().getHostAddress();
        return setDNSRecordToValue(hostedZoneId, hostname, ipAddressAsString);
    }
    
    @Override
    public ChangeInfo setDNSRecordToApplicationLoadBalancer(String hostedZoneId, String hostname,
            ApplicationLoadBalancer<ShardingKey, MetricsT> alb) {
        final String dnsName = alb.getDNSName();
        return setDNSRecord(hostedZoneId, hostname, RRType.CNAME, dnsName);
    }

    @Override
    public ChangeInfo setDNSRecordToValue(String hostedZoneId, String hostname, String value) {
        return setDNSRecord(hostedZoneId, hostname, RRType.A, value);
    }

    @Override
    public String getDNSHostedZoneId(String hostedZoneName) {
        return getRoute53Client().listHostedZonesByName(b->b.dnsName(hostedZoneName)).hostedZones().iterator().next().id().replaceFirst("^\\/hostedzone\\/", "");
    }

    private ChangeInfo setDNSRecord(String hostedZoneId, String hostname, RRType type, String value) {
        final ChangeResourceRecordSetsResponse response = getRoute53Client()
                .changeResourceRecordSets(
                        ChangeResourceRecordSetsRequest.builder().hostedZoneId(hostedZoneId)
                                .changeBatch(ChangeBatch.builder().changes(Change.builder().action(ChangeAction.UPSERT)
                                        .resourceRecordSet(ResourceRecordSet.builder().name(hostname).type(type).ttl(DEFAULT_DNS_TTL_MILLIS)
                                                .resourceRecords(ResourceRecord.builder().value(value).build()).build())
                                        .build()).build())
                                .build());
        return response.changeInfo();
    }

    @Override
    public ChangeInfo removeDNSRecord(String hostedZoneId, String hostname, String value) {
        return removeDNSRecord(hostedZoneId, hostname, RRType.A, value);
    }

    @Override
    public ChangeInfo removeDNSRecord(String hostedZoneId, String hostname, RRType type, String value) {
        return getRoute53Client().changeResourceRecordSets(ChangeResourceRecordSetsRequest.builder().hostedZoneId(hostedZoneId)
                .changeBatch(ChangeBatch.builder().changes(Change.builder().action(ChangeAction.DELETE)
                        .resourceRecordSet(ResourceRecordSet.builder().name(hostname).type(type).ttl(DEFAULT_DNS_TTL_MILLIS)
                                .resourceRecords(ResourceRecord.builder().value(value).build()).build()).build()).build()).build()).
                changeInfo();
    }

    @Override
    public ChangeInfo getUpdatedChangeInfo(ChangeInfo changeInfo) {
        return getRoute53Client().getChange(GetChangeRequest.builder().id(changeInfo.id()).build()).changeInfo();
    }

    @Override
    public AmazonMachineImage<ShardingKey, MetricsT> getImage(com.sap.sse.landscape.Region region, String imageId) {
        final DescribeImagesResponse response = getEc2Client(getRegion(region))
                .describeImages(DescribeImagesRequest.builder().imageIds(imageId).build());
        return new AmazonMachineImageImpl<>(response.images().iterator().next(), region, this);
    }
    
    @Override
    public AmazonMachineImage<ShardingKey, MetricsT> createImage(AwsInstance<ShardingKey, MetricsT> instance, String imageName, Optional<Tags> tags) {
        logger.info("Creating Amazon Machine Image (AMI) named "+imageName+" for instance "+instance.getInstanceId());
        final Ec2Client client = getEc2Client(getRegion(instance.getRegion()));
        final String imageId = client.createImage(b->b
                .instanceId(instance.getInstanceId())
                .name(imageName)).imageId();
        final CreateTagsRequest.Builder createTagsRequestBuilder = CreateTagsRequest.builder().resources(imageId);
        // Apply the tags if present
        tags.ifPresent(t->t.forEach(tag->createTagsRequestBuilder.tags(Tag.builder().key(tag.getKey()).value(tag.getValue()).build())));
        client.createTags(createTagsRequestBuilder.build());
        return getImage(instance.getRegion(), imageId);
    }

    @Override
    public void deleteImage(com.sap.sse.landscape.Region region, String imageId) {
        getEc2Client(getRegion(region)).deregisterImage(b->b.imageId(imageId));
    }

    @Override
    public AmazonMachineImage<ShardingKey, MetricsT> getLatestImageWithTag(com.sap.sse.landscape.Region region, String tagName, String tagValue) {
        final DescribeImagesResponse response = getEc2Client(getRegion(region))
                .describeImages(DescribeImagesRequest.builder().filters(Filter.builder().name("tag:"+tagName).values(tagValue).build()).build());
        return new AmazonMachineImageImpl<>(response.images().stream().max(getMachineImageCreationDateComparator()).get(), region, this);
    }
    
    @Override
    public Iterable<String> getMachineImageTypes(com.sap.sse.landscape.Region region) {
        final DescribeImagesResponse response = getEc2Client(getRegion(region))
                .describeImages(DescribeImagesRequest.builder().filters(
                        Filter.builder().name("tag-key").values(IMAGE_TYPE_TAG_NAME).build()).build());
        return Util.map(response.images(), image->image.tags().stream().filter(t->t.key().equals(IMAGE_TYPE_TAG_NAME)).findAny().get().value());
    }

    @Override
    public void setSnapshotName(com.sap.sse.landscape.Region region, String snapshotId, String snapshotName) {
        getEc2Client(getRegion(region)).createTags(b->b
                .resources(snapshotId)
                .tags(Tag.builder().key("Name").value(snapshotName).build()));
    }

    @Override
    public void deleteSnapshot(com.sap.sse.landscape.Region region, String snapshotId) {
        getEc2Client(getRegion(region)).deleteSnapshot(b->b.snapshotId(snapshotId));
    }

    @Override
    public Iterable<AwsInstance<ShardingKey, MetricsT>> getHostsWithTagValue(com.sap.sse.landscape.Region region,
            String tagName, String tagValue) {
        Filter filter = getHostWithTagValueFilter(tagName, tagValue).build();
        return getHostsWithFilters(region, filter);
    }

    private Filter.Builder getHostWithTagValueFilter(String tagName, String tagValue) {
        return Filter.builder().name("tag:"+tagName).values(tagValue);
    }

    @Override
    public Iterable<AwsInstance<ShardingKey, MetricsT>> getRunningHostsWithTagValue(com.sap.sse.landscape.Region region,
            String tagName, String tagValue) {
        return getHostsWithFilters(region, getRunningHostFilter());
    }
    
    private Filter getRunningHostFilter() {
        return Filter.builder().name("instance-state-name").values("running").build();
    }

    private Iterable<AwsInstance<ShardingKey, MetricsT>> getHostsWithFilters(com.sap.sse.landscape.Region region, Filter... filters) {
        final List<AwsInstance<ShardingKey, MetricsT>> result = new ArrayList<>();
        final DescribeInstancesResponse instanceResponse = getEc2Client(getRegion(region)).describeInstances(b->b.filters(filters));
        for (final Reservation r : instanceResponse.reservations()) {
            for (final Instance i : r.instances()) {
                result.add(getHost(region, i));
            }
        }
        return result;
    }

    private AwsInstance<ShardingKey, MetricsT> getHost(com.sap.sse.landscape.Region region, final Instance instance) {
        return new AwsInstanceImpl<ShardingKey, MetricsT>(instance.instanceId(), getAvailabilityZoneByName(region, instance.placement().availabilityZone()), this);
    }

    private AwsInstance<ShardingKey, MetricsT> getHost(com.sap.sse.landscape.Region region, final String instanceId) {
        return getHost(region, getInstance(instanceId, region));
    }
    
    @Override
    public Iterable<AwsInstance<ShardingKey, MetricsT>> getRunningHostsWithTag(com.sap.sse.landscape.Region region, String tagName) {
        return getHostsWithFilters(region, getFilterForHostWithTag(Filter.builder(), tagName), getRunningHostFilter());
    }
    
    @Override
    public Iterable<AwsInstance<ShardingKey, MetricsT>> getHostsWithTag(com.sap.sse.landscape.Region region, String tagName) {
        return getHostsWithFilters(region, getFilterForHostWithTag(Filter.builder(), tagName));
    }
    
    private Filter getFilterForHostWithTag(Filter.Builder builder, String tagName) {
        return builder.name("tag-key").values(tagName).build();
    }

    private Comparator<? super Image> getMachineImageCreationDateComparator() {
        return (ami1, ami2)->{
            return ami1.creationDate().compareTo(ami2.creationDate());
        };
    }

    private AwsCredentials getCredentials() {
        return AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }
    
    @Override
    public KeyPairInfo getKeyPairInfo(com.sap.sse.landscape.Region region, String keyName) {
        return getEc2Client(getRegion(region))
                .describeKeyPairs(DescribeKeyPairsRequest.builder().keyNames(keyName).build()).keyPairs().iterator()
                .next();
    }

    @Override
    public Iterable<KeyPairInfo> getAllKeyPairInfos(com.sap.sse.landscape.Region region) {
        return getEc2Client(getRegion(region))
                .describeKeyPairs(DescribeKeyPairsRequest.builder().build()).keyPairs();
    }

    @Override
    public void deleteKeyPair(com.sap.sse.landscape.Region region, String keyName) {
        getEc2Client(getRegion(region)).deleteKeyPair(DeleteKeyPairRequest.builder().keyName(keyName).build());
        mongoObjectFactory.removeSSHKeyPair(region.getId(), keyName);
    }

    @Override
    public String importKeyPair(com.sap.sse.landscape.Region region, byte[] publicKey, byte[] unencryptedPrivateKey, String keyName) throws JSchException {
        final String keyId = getEc2Client(getRegion(region)).importKeyPair(ImportKeyPairRequest.builder().keyName(keyName)
                .publicKeyMaterial(SdkBytes.fromByteArray(publicKey)).build()).keyPairId();
        Object principal;
        try {
            principal = SessionUtils.getPrincipal();
        } catch (Exception e) {
            logger.severe("Couldn't find current user; continuing anonymously");
            principal = null;
        }
        final SSHKeyPair keyPair = new SSHKeyPair(region.getId(), principal==null?"":principal.toString(),
                TimePoint.now(), keyName, publicKey, unencryptedPrivateKey, privateKeyEncryptionPassphrase);
        addSSHKeyPair(keyPair);
        return keyId;
    }

    @Override
    public SSHKeyPair getSSHKeyPair(com.sap.sse.landscape.Region region, String keyName) {
        return sshKeyPairs.get(new Pair<>(region.getId(), keyName));
    }

    @Override
    public byte[] getDecryptedPrivateKey(SSHKeyPair keyPair) throws JSchException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        keyPair.getKeyPair(new JSch(), privateKeyEncryptionPassphrase).writePrivateKey(bos);
        return bos.toByteArray();
    }

    @Override
    public Map<Scope<ShardingKey>, ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT>> getScopes() {
        // TODO Implement Landscape<ShardingKey,MetricsT>.getScopes(...)
        return null;
    }
    
    @Override
    public <HostT extends AwsInstance<ShardingKey, MetricsT>> Iterable<HostT> launchHosts(HostSupplier<ShardingKey, MetricsT, ProcessT, HostT> hostSupplier,
            int numberOfHostsToLaunch, MachineImage fromImage,
            InstanceType instanceType, AwsAvailabilityZone az, String keyName, Iterable<SecurityGroup> securityGroups, Optional<Tags> tags, String... userData) {
        if (!fromImage.getRegion().equals(az.getRegion())) {
            throw new IllegalArgumentException("Trying to launch an instance in region "+az.getRegion()+
                    " with image "+fromImage+" that lives in region "+fromImage.getRegion()+" which is different."+
                    " Consider copying the image to that region.");
        }
        final Builder runInstancesRequestBuilder = RunInstancesRequest.builder()
            .additionalInfo("Test " + getClass().getName())
            .imageId(fromImage.getId().toString())
            .minCount(numberOfHostsToLaunch)
            .maxCount(numberOfHostsToLaunch)
            .instanceType(instanceType).keyName(keyName)
            .placement(Placement.builder().availabilityZone(az.getName()).build())
            .securityGroupIds(Util.mapToArrayList(securityGroups, sg->sg.getId()));
        if (userData != null) {
            runInstancesRequestBuilder.userData(Base64.getEncoder().encodeToString(String.join("\n", userData).getBytes()));
        }
        tags.ifPresent(theTags->{
            final List<Tag> awsTags = new ArrayList<>();
            for (final Entry<String, String> tag : theTags) {
                awsTags.add(Tag.builder().key(tag.getKey()).value(tag.getValue()).build());
            }
            runInstancesRequestBuilder.tagSpecifications(TagSpecification.builder().resourceType(ResourceType.INSTANCE).tags(awsTags).build());
        });
        final RunInstancesRequest launchRequest = runInstancesRequestBuilder.build();
        logger.info("Launching instance(s): "+launchRequest);
        final RunInstancesResponse response = getEc2Client(getRegion(az.getRegion())).runInstances(launchRequest);
        final List<HostT> result = new ArrayList<>();
        for (final Instance instance : response.instances()) {
            result.add(hostSupplier.supply(instance.instanceId(), az, this));
        }
        return result;
    }

    @Override
    public void terminate(AwsInstance<ShardingKey, MetricsT> host) {
        logger.info("Terminating instance "+host);
        getEc2Client(getRegion(host.getAvailabilityZone().getRegion())).terminateInstances(
                TerminateInstancesRequest.builder().instanceIds(host.getInstanceId()).build());
    }

    private Region getRegion(com.sap.sse.landscape.Region region) {
        return Region.of(region.getId());
    }

    @Override
    public Iterable<AvailabilityZone> getAvailabilityZones(com.sap.sse.landscape.Region awsRegion) {
        return Util.map(getEc2Client(getRegion(awsRegion)).describeAvailabilityZones().availabilityZones(),
                AwsAvailabilityZoneImpl::new);
    }

    @Override
    public TargetGroup<ShardingKey, MetricsT> getTargetGroup(com.sap.sse.landscape.Region region, String targetGroupName) {
        final ElasticLoadBalancingV2Client loadBalancingClient = getLoadBalancingClient(getRegion(region));
        final DescribeTargetGroupsResponse targetGroupResponse = loadBalancingClient.describeTargetGroups(b->b.names(targetGroupName));
        return targetGroupResponse.hasTargetGroups()
                ? new AwsTargetGroupImpl<>(this, region, targetGroupName,
                        targetGroupResponse.targetGroups().iterator().next().targetGroupArn())
                : null;
    }

    @Override
    public TargetGroup<ShardingKey, MetricsT> createTargetGroup(com.sap.sse.landscape.Region region, String targetGroupName, int port, String healthCheckPath, int healthCheckPort) {
        final ElasticLoadBalancingV2Client loadBalancingClient = getLoadBalancingClient(getRegion(region));
        final software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup targetGroup =
                loadBalancingClient.createTargetGroup(CreateTargetGroupRequest.builder()
                .name(targetGroupName)
                .healthyThresholdCount(2)
                .unhealthyThresholdCount(2)
                .healthCheckTimeoutSeconds(4)
                .healthCheckEnabled(true)
                .healthCheckIntervalSeconds(5)
                .healthCheckPath(healthCheckPath)
                .healthCheckPort(""+healthCheckPort)
                .healthCheckProtocol(guessProtocolFromPort(healthCheckPort))
                .port(port)
                .vpcId(getVpcId(region))
                .protocol(guessProtocolFromPort(port))
                .targetType(TargetTypeEnum.INSTANCE)
                .build()).targetGroups().iterator().next();
        final String targetGroupArn = targetGroup.targetGroupArn();
        loadBalancingClient.modifyTargetGroupAttributes(ModifyTargetGroupAttributesRequest.builder()
                .targetGroupArn(targetGroupArn)
                .attributes(TargetGroupAttribute.builder().key("stickiness.enabled").value("true").build(),
                            TargetGroupAttribute.builder().key("load_balancing.algorithm.type").value("least_outstanding_requests")
                            .build()).build());
        return new AwsTargetGroupImpl<>(this, region, targetGroupName, targetGroupArn);
    }

    private ProtocolEnum guessProtocolFromPort(int healthCheckPort) {
        return healthCheckPort == 443 ? ProtocolEnum.HTTPS : ProtocolEnum.HTTP;
    }
    
    private String getVpcId(com.sap.sse.landscape.Region region) {
        Vpc vpc = getEc2Client(getRegion(region)).describeVpcs().vpcs().stream().filter(myVpc->myVpc.isDefault()).findAny().
                orElseThrow(()->new IllegalStateException("No default VPC found in region "+region));
        return vpc.vpcId();
    }

    @Override
    public software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup getAwsTargetGroup(com.sap.sse.landscape.Region region, String targetGroupName) {
        return getLoadBalancingClient(getRegion(region)).describeTargetGroups(DescribeTargetGroupsRequest.builder()
                        .names(targetGroupName).build()).targetGroups().iterator().next();
    }
    
    @Override
    public software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup getAwsTargetGroupByArn(com.sap.sse.landscape.Region region, String targetGroupArn) {
        return getLoadBalancingClient(getRegion(region)).describeTargetGroups(DescribeTargetGroupsRequest.builder()
                        .targetGroupArns(targetGroupArn).build()).targetGroups().iterator().next();
    }
    
    @Override
    public <SK, MT extends ApplicationProcessMetrics> void deleteTargetGroup(TargetGroup<SK, MT> targetGroup) {
        getLoadBalancingClient(getRegion(targetGroup.getRegion())).deleteTargetGroup(DeleteTargetGroupRequest.builder().targetGroupArn(
                targetGroup.getTargetGroupArn()).build());
    }

    @Override
    public Map<AwsInstance<ShardingKey, MetricsT>, TargetHealth> getTargetHealthDescriptions(TargetGroup<ShardingKey, MetricsT> targetGroup) {
        final Map<AwsInstance<ShardingKey, MetricsT>, TargetHealth> result = new HashMap<>();
        final Region region = getRegion(targetGroup.getRegion());
        getLoadBalancingClient(region)
                .describeTargetHealth(DescribeTargetHealthRequest.builder().targetGroupArn(targetGroup.getTargetGroupArn()).build())
                .targetHealthDescriptions().forEach(
                    targetHealthDescription->result.put(
                            getHost(targetGroup.getRegion(), targetHealthDescription.target().id()), targetHealthDescription.targetHealth()));
        return result;
    }

    @Override
    public ReverseProxyCluster<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> getCentralReverseProxy(com.sap.sse.landscape.Region region) {
        ApacheReverseProxyCluster<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> reverseProxyCluster = new ApacheReverseProxyCluster<>(this);
        for (final AwsInstance<ShardingKey, MetricsT> reverseProxyHost : getRunningHostsWithTag(region, CENTRAL_REVERSE_PROXY_TAG_NAME)) {
            reverseProxyCluster.addHost(reverseProxyHost);
        }
        return reverseProxyCluster;
    }

    @Override
    public SecurityGroup getSecurityGroup(String securityGroupId, com.sap.sse.landscape.Region region) {
        return ()->getEc2Client(getRegion(region)).describeSecurityGroups(sg->sg.groupIds(securityGroupId)).securityGroups().iterator().next().groupId();
    }

    @Override
    public void addTargetsToTargetGroup(
            TargetGroup<ShardingKey, MetricsT> targetGroup,
            Iterable<AwsInstance<ShardingKey, MetricsT>> targets) {
        getLoadBalancingClient(getRegion(targetGroup.getRegion())).registerTargets(getRegisterTargetsRequestBuilderConsumer(targetGroup, targets));
    }

    private TargetDescription[] getTargetDescriptions(Iterable<AwsInstance<ShardingKey, MetricsT>> targets) {
        return Util.toArray(Util.map(targets, t->TargetDescription.builder().id(t.getInstanceId()).build()), new TargetDescription[0]);
    }

    @Override
    public void removeTargetsFromTargetGroup(
            TargetGroup<ShardingKey, MetricsT> targetGroup,
            Iterable<AwsInstance<ShardingKey, MetricsT>> targets) {
        getLoadBalancingClient(getRegion(targetGroup.getRegion())).deregisterTargets(getDeregisterTargetRequestBuilderConsumers(targetGroup, targets));
    }

    private Consumer<software.amazon.awssdk.services.elasticloadbalancingv2.model.RegisterTargetsRequest.Builder> getRegisterTargetsRequestBuilderConsumer(
            TargetGroup<ShardingKey, MetricsT> targetGroup, Iterable<AwsInstance<ShardingKey, MetricsT>> targets) {
        final TargetDescription[] targetDescriptions = getTargetDescriptions(targets);
        return b->b
                .targetGroupArn(targetGroup.getTargetGroupArn())
                .targets(targetDescriptions);
    }

    private Consumer<software.amazon.awssdk.services.elasticloadbalancingv2.model.DeregisterTargetsRequest.Builder> getDeregisterTargetRequestBuilderConsumers(
            TargetGroup<ShardingKey, MetricsT> targetGroup, Iterable<AwsInstance<ShardingKey, MetricsT>> targets) {
        final TargetDescription[] targetDescriptions = getTargetDescriptions(targets);
        return b->b
                .targetGroupArn(targetGroup.getTargetGroupArn())
                .targets(targetDescriptions);
    }

    @Override
    public LoadBalancer getAwsLoadBalancer(String loadBalancerArn, com.sap.sse.landscape.Region region) {
        return getLoadBalancingClient(getRegion(region))
                .describeLoadBalancers(lb -> lb.loadBalancerArns(loadBalancerArn)).loadBalancers().iterator().next();
    }

    @Override
    public SecurityGroup getDefaultSecurityGroupForApplicationHosts(com.sap.sse.landscape.Region region) {
        final SecurityGroup result;
        // TODO find a better way, e.g., by tagging, to identify the security group per region to use for application hosts
        if (region.getId().equals(Region.EU_WEST_1.id())) {
            result = getSecurityGroup(DEFAULT_APPLICATION_SERVER_SECURITY_GROUP_ID_EU_WEST_1, region);
        } else if (region.getId().equals(Region.EU_WEST_2.id())) {
            result = getSecurityGroup(DEFAULT_APPLICATION_SERVER_SECURITY_GROUP_ID_EU_WEST_2, region);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public SecurityGroup getDefaultSecurityGroupForCentralReverseProxy(com.sap.sse.landscape.Region region) {
        return getDefaultSecurityGroupForApplicationHosts(region);
    }

    @Override
    public SecurityGroup getDefaultSecurityGroupForMongoDBHosts(com.sap.sse.landscape.Region region) {
        final SecurityGroup result;
        // TODO find a better way, e.g., by tagging, to identify the security group per region to use for MongoDB hosts
        if (region.getId().equals(Region.EU_WEST_1.id())) {
            result = getSecurityGroup(DEFAULT_MONGODB_SECURITY_GROUP_ID_EU_WEST_1, region);
        } else if (region.getId().equals(Region.EU_WEST_2.id())) {
            result = getSecurityGroup(DEFAULT_MONGODB_SECURITY_GROUP_ID_EU_WEST_2, region);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public ApplicationLoadBalancer<ShardingKey, MetricsT> getNonDNSMappedLoadBalancer(
            com.sap.sse.landscape.Region region, String wildcardDomain) {
        return getLoadBalancerByName(getNonDNSMappedLoadBalancerName(wildcardDomain), region);
    }

    @Override
    public ApplicationLoadBalancer<ShardingKey, MetricsT> createNonDNSMappedLoadBalancer(
            com.sap.sse.landscape.Region region, String wildcardDomain) {
        return createLoadBalancer(getNonDNSMappedLoadBalancerName(wildcardDomain), region);
    }

    private String getNonDNSMappedLoadBalancerName(String wildcardDomain) {
        return DEFAULT_NON_DNS_MAPPED_ALB_NAME + wildcardDomain.replaceAll("\\.", "-");
    }

    @Override
    public ApplicationLoadBalancer<ShardingKey, MetricsT> getDNSMappedLoadBalancerFor(
            com.sap.sse.landscape.Region region, String hostname) {
        final DescribeLoadBalancersResponse response = getLoadBalancingClient(getRegion(region)).describeLoadBalancers();
        for (final LoadBalancer lb : response.loadBalancers()) {
            final ApplicationLoadBalancer<ShardingKey, MetricsT> alb = new ApplicationLoadBalancerImpl<>(region, lb, this);
            for (final Rule rule : alb.getRules()) {
                if (rule.conditions().stream().filter(r->r.hostHeaderConfig().values().contains(hostname)).findAny().isPresent()) {
                    return alb;
                }
            }
        }
        return null;
    }
    
    @Override
    public MongoEndpoint getDatabaseConfigurationForDefaultReplicaSet(com.sap.sse.landscape.Region region) {
        return getDatabaseConfigurationForReplicaSet(region, MONGO_DEFAULT_REPLICA_SET_NAME);
    }
    
    private int getMongoPort(String[] replicaSetNameAndOptionalPort) {
        final int result;
        if (replicaSetNameAndOptionalPort.length < 2) {
            result = MongoProcess.DEFAULT_PORT;
        } else {
            result = Integer.valueOf(replicaSetNameAndOptionalPort[1].trim());
        }
        return result;
    }

    @Override
    public Optional<String> getTag(AwsInstance<ShardingKey, MetricsT> host, String tagName) {
        final DescribeTagsResponse tagResponse = getEc2Client(getRegion(host.getRegion())).describeTags(b->b.filters(
                Filter.builder()
                    .name("resource-id").values(host.getInstanceId()).build(),
                Filter.builder()
                    .name("key").values(tagName).build()));
        return tagResponse.tags().stream().map(t->t.value()).findAny();
    }

    @Override
    public Tags getTagForMongoProcess(Tags tagsToAddTo, String replicaSetName, int port) {
        return tagsToAddTo.and(MONGO_REPLICA_SETS_TAG_NAME,
                (replicaSetName==null?"":replicaSetName)+MONGO_REPLICA_SET_NAME_AND_PORT_SEPARATOR+port);
    }

    @Override
    public MongoReplicaSet getDatabaseConfigurationForReplicaSet(com.sap.sse.landscape.Region region, String mongoReplicaSetName) {
        final MongoReplicaSet result = new MongoReplicaSetImpl(mongoReplicaSetName);
        for (final AwsInstance<ShardingKey, MetricsT> host : getMongoDBHosts(region)) {
            for (final Pair<String, Integer> replicaSetNameAndPort : getMongoEndpointSpecificationsAsReplicaSetNameAndPort(host)) {
                if (replicaSetNameAndPort.getA().equals(mongoReplicaSetName)) {
                    result.addReplica(new MongoProcessInReplicaSetImpl(result, replicaSetNameAndPort.getB(), host));
                }
            }
        }
        return result;
    }

    private Iterable<AwsInstance<ShardingKey, MetricsT>> getMongoDBHosts(com.sap.sse.landscape.Region region) {
        return getRunningHostsWithTag(region, MONGO_REPLICA_SETS_TAG_NAME);
    }

    /**
     * @param host
     *            assumed to be a host that has the {@link #MONGO_REPLICA_SETS_TAG_NAME} tag set
     * @return the replica set name / port number pairs extracted from the tag value
     */
    private Iterable<Pair<String, Integer>> getMongoEndpointSpecificationsAsReplicaSetNameAndPort(final AwsInstance<ShardingKey, MetricsT> host) {
        final List<Pair<String, Integer>> result = new ArrayList<>();
        getTag(host, MONGO_REPLICA_SETS_TAG_NAME).ifPresent(tagValue->{
            for (final String replicaNameWithOptionalPortColonSeparated : tagValue.split(",")) {
                final String[] splitByColon = replicaNameWithOptionalPortColonSeparated.split(MONGO_REPLICA_SET_NAME_AND_PORT_SEPARATOR);
                final int port = getMongoPort(splitByColon);
                result.add(new Pair<>(splitByColon[0].trim(), port));
            }});
        return result;
    }

    @Override
    public Iterable<MongoEndpoint> getMongoEndpoints(com.sap.sse.landscape.Region region) {
        final Set<MongoEndpoint> result = new HashSet<>();
        final Set<String> replicaSetsCreated = new HashSet<>();
        for (final AwsInstance<ShardingKey, MetricsT> mongoDBHost : getMongoDBHosts(region)) {
            for (final Pair<String, Integer> replicaSetNameAndPort : getMongoEndpointSpecificationsAsReplicaSetNameAndPort(mongoDBHost)) {
                if (replicaSetNameAndPort.getA() != null && !replicaSetNameAndPort.getA().isEmpty()) { // non-empty replica set name
                    if (!replicaSetsCreated.contains(replicaSetNameAndPort.getA())) {
                        replicaSetsCreated.add(replicaSetNameAndPort.getA());
                        result.add(getDatabaseConfigurationForReplicaSet(region, replicaSetNameAndPort.getA()));
                    }
                } else {
                    // single instance:
                    result.add(new MongoProcessImpl(replicaSetNameAndPort.getB(), mongoDBHost));
                }
            }
        }
        return result;
    }

    @Override
    public RabbitMQEndpoint getDefaultRabbitConfiguration(AwsRegion region) {
        final RabbitMQEndpoint result;
        final Iterable<AwsInstance<ShardingKey, MetricsT>> rabbitMQHostsInRegion = getRunningHostsWithTag(region, RABBITMQ_TAG_NAME);
        if (rabbitMQHostsInRegion.iterator().hasNext()) {
            final AwsInstance<ShardingKey, MetricsT> anyRabbitMQHost = rabbitMQHostsInRegion.iterator().next();
            result = new RabbitMQEndpoint() {
                @Override
                public int getPort() {
                    return getTag(anyRabbitMQHost, RABBITMQ_TAG_NAME)
                            .map(t -> t.trim().isEmpty() ? RabbitMQEndpoint.DEFAULT_PORT : Integer.valueOf(t.trim()))
                            .orElse(RabbitMQEndpoint.DEFAULT_PORT);
                }

                @Override
                public String getNodeName() {
                    return anyRabbitMQHost.getPublicAddress().getCanonicalHostName();
                }
            };
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Database getDatabase(com.sap.sse.landscape.Region region, String databaseName) {
        return new DatabaseImpl(getDatabaseConfigurationForDefaultReplicaSet(region), databaseName);
    }

    @Override
    public RabbitMQEndpoint getMessagingConfigurationForDefaultCluster(com.sap.sse.landscape.Region region) {
        final RabbitMQEndpoint result;
        if (region.getId().equals(Region.EU_WEST_1.id())) {
            result = ()->"rabbit.internal.sapsailing.com";
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    public Iterable<ApplicationProcessHost<ShardingKey, MetricsT, ProcessT>> getApplicationProcessHostsByTag(com.sap.sse.landscape.Region region, String tagName,
            BiFunction<Host, String, ProcessT> processFactoryFromHostAndServerDirectory) {
        final List<ApplicationProcessHost<ShardingKey, MetricsT, ProcessT>> result = new ArrayList<>();
        for (final AwsInstance<ShardingKey, MetricsT> host : getRunningHostsWithTag(region, tagName)) {
            final ApplicationProcessHost<ShardingKey, MetricsT, ProcessT> applicationProcessHost =
                    new ApplicationProcessHostImpl<ShardingKey, MetricsT, ProcessT>(
                            host.getInstanceId(), host.getAvailabilityZone(), this, processFactoryFromHostAndServerDirectory);
            result.add(applicationProcessHost);
        }
        return result;
    }
    
    @Override
    public Iterable<ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT>> getApplicationReplicaSetsByTag(com.sap.sse.landscape.Region region, String tagName,
            BiFunction<Host, String, ProcessT> processFactoryFromHostAndServerDirectory, Optional<Duration> optionalTimeout) throws Exception {
        final Iterable<ApplicationProcessHost<ShardingKey, MetricsT, ProcessT>> hosts = getApplicationProcessHostsByTag(region, tagName, processFactoryFromHostAndServerDirectory);
        final Map<String, ProcessT> mastersByServerName = new HashMap<>();
        final Map<String, Set<ProcessT>> replicasByServerName = new HashMap<>();
        for (final ApplicationProcessHost<ShardingKey, MetricsT, ProcessT> host : hosts) {
            for (final ProcessT applicationProcess : host.getApplicationProcesses(optionalTimeout)) {
                final String serverName = applicationProcess.getServerName(optionalTimeout);
                final String masterServerName = applicationProcess.getMasterServerName(optionalTimeout);
                if (masterServerName != null && Util.equalsWithNull(masterServerName, serverName)) {
                    // then applicationProcess is a replica in the serverName cluster:
                    Util.addToValueSet(replicasByServerName, serverName, applicationProcess);
                } else {
                    mastersByServerName.put(serverName, applicationProcess);
                }
            }
        }
        final Set<ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT>> result = new HashSet<>();
        for (final Entry<String, ProcessT> serverNameAndMaster : mastersByServerName.entrySet()) {
            final ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSet = new ApplicationReplicaSetImpl<>(serverNameAndMaster.getKey(), serverNameAndMaster.getValue(),
                    Optional.ofNullable(replicasByServerName.get(serverNameAndMaster.getKey())));
            result.add(replicaSet);
        }
        return result;
    }

    @Override
    public AwsRegion getDefaultRegion() {
        return new AwsRegion(Region.EU_WEST_2); // TODO actually, EU_WEST_1 (Ireland) is our default region, but as long as this is under development, EU_WEST_2 gives us an isolated test environment
    }

    @Override
    public Iterable<com.sap.sse.landscape.Region> getRegions() {
        return Util.map(Region.regions(), AwsRegion::new);
    }
}
