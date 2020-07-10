package com.sap.sse.landscape.aws.impl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.persistence.DomainObjectFactory;
import com.sap.sse.landscape.aws.persistence.MongoObjectFactory;
import com.sap.sse.landscape.aws.persistence.PersistenceFactory;
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
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsRequest;
import software.amazon.awssdk.services.ec2.model.ImportKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.ec2.model.Placement;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
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

public class AwsLandscapeImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics> implements AwsLandscape<ShardingKey, MetricsT> {
    private static final Logger logger = Logger.getLogger(AwsLandscapeImpl.class.getName());
    private static final long DEFAULT_DNS_TTL_MILLIS = 60000l;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final MongoObjectFactory mongoObjectFactory;
    private ConcurrentMap<Pair<String, String>, SSHKeyPair> sshKeyPairs;
    private final AwsRegion globalRegion;
    
    /**
     * Used for the symmetric encryption / decryption of private SSH keys. See also
     * {@link #getDescryptedPrivateKey(SSHKeyPair)}.
     */
    private final byte[] privateKeyEncryptionPassphrase;
    
    public AwsLandscapeImpl() {
        this(System.getProperty(ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME), System.getProperty(SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME),
                // by using MongoDBService.INSTANCE the default test configuration will be used if nothing else is configured
                PersistenceFactory.INSTANCE.getDomainObjectFactory(MongoDBService.INSTANCE),
                PersistenceFactory.INSTANCE.getMongoObjectFactory(MongoDBService.INSTANCE));
    }
    
    public AwsLandscapeImpl(String accessKeyId, String secretAccessKey,
            DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) {
        this.privateKeyEncryptionPassphrase = ("aw4raif87l"+"098sf;;50").getBytes();
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.globalRegion = new AwsRegion(Region.AWS_GLOBAL);
        this.mongoObjectFactory = mongoObjectFactory;
        this.sshKeyPairs = new ConcurrentHashMap<Util.Pair<String,String>, SSHKeyPair>();
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
    
    @Override
    public void addSSHKeyPair(SSHKeyPair keyPair) {
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
            ApplicationLoadBalancer alb) {
        final String dnsName = alb.getDNSName();
        return setDNSRecord(hostedZoneId, hostname, RRType.CNAME, dnsName);
    }

    @Override
    public ChangeInfo setDNSRecordToValue(String hostedZoneId, String hostname, String value) {
        return setDNSRecord(hostedZoneId, hostname, RRType.A, value);
    }

    // TODO should the default DNS hosted zone ID for a landscape be configurable? persistent? A property at all?
    @Override
    public String getDefaultDNSHostedZoneId() {
//      final String hostedZoneId = "Z2JYWXYWLLRLTE"; // TODO sapsailing.com.
        return "Z1Z1ID6TP8HVB2"; // TODO test zone "wiesen-weg.de."
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
    public AmazonMachineImage getImage(com.sap.sse.landscape.Region region, String imageId) {
        final DescribeImagesResponse response = getEc2Client(getRegion(region))
                .describeImages(DescribeImagesRequest.builder().imageIds(imageId).build());
        return new AmazonMachineImage(response.images().iterator().next(), region);
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
    public byte[] getDescryptedPrivateKey(SSHKeyPair keyPair) throws JSchException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        keyPair.getKeyPair(new JSch(), privateKeyEncryptionPassphrase).writePrivateKey(bos);
        return bos.toByteArray();
    }

    @Override
    public Map<Scope<ShardingKey>, ApplicationReplicaSet<ShardingKey, MetricsT>> getScopes() {
        // TODO Implement Landscape<ShardingKey,MetricsT>.getScopes(...)
        return null;
    }

    @Override
    public Iterable<AwsInstance> launchHosts(int numberOfHostsToLaunch, MachineImage<AwsInstance> fromImage, AvailabilityZone az,
            String keyName, Iterable<SecurityGroup> securityGroups) {
        if (!fromImage.getRegion().equals(az.getRegion())) {
            throw new IllegalArgumentException("Trying to launch an instance in region "+az.getRegion()+
                    " with image "+fromImage+" that lives in region "+fromImage.getRegion()+" which is different."+
                    " Consider copying the image to that region.");
        }
        final RunInstancesRequest launchRequest = RunInstancesRequest.builder()
            .additionalInfo("Test " + getClass().getName())
            .imageId(fromImage.getId().toString())
            .minCount(numberOfHostsToLaunch)
            .maxCount(numberOfHostsToLaunch)
            .instanceType(InstanceType.T3_SMALL).keyName(keyName)
            .placement(Placement.builder().availabilityZone(az.getId()).build())
            .securityGroupIds(Util.mapToArrayList(securityGroups, sg->sg.getId())).build();
        logger.info("Launching instance(s): "+launchRequest);
        final RunInstancesResponse response = getEc2Client(getRegion(az.getRegion())).runInstances(launchRequest);
        final List<AwsInstance> result = new ArrayList<>();
        for (final Instance instance : response.instances()) {
            result.add(new AwsInstanceImpl(instance.instanceId(), az, this));
        }
        return result;
    }

    @Override
    public void terminate(AwsInstance host) {
        logger.info("Terminating instance "+host);
        getEc2Client(getRegion(host.getAvailabilityZone().getRegion())).terminateInstances(
                TerminateInstancesRequest.builder().instanceIds(host.getInstanceId()).build());
    }

    private Region getRegion(com.sap.sse.landscape.Region region) {
        return Region.of(region.getId());
    }
}
