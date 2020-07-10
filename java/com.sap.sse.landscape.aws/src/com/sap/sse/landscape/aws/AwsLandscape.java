package com.sap.sse.landscape.aws;

import com.jcraft.jsch.JSchException;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.impl.AwsLandscapeImpl;
import com.sap.sse.landscape.ssh.SSHKeyPair;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.ChangeInfo;
import software.amazon.awssdk.services.route53.model.RRType;

/**
 * A simplified view onto the AWS SDK API that is geared towards specific ways and patterns of managing an application
 * and infrastructure landscape. Among others, it uses {@link Ec2Client}, {@link Route53Client},
 * {@link CloudWatchClient} and {@link ElasticLoadBalancingV2Client} to manage the underlying AWS landscape.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 */
public interface AwsLandscape<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends Landscape<ShardingKey, MetricsT> {
    static String ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME = "com.sap.sse.landscape.aws.accesskeyid";

    static String SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME = "com.sap.sse.landscape.aws.secretaccesskey";

    /**
     * Based on system properties for the AWS access key ID and the secret access key (see
     * {@link #ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME} and {@link #SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME}), this method
     * returns a landscape object which internally has access to the clients for the underlying AWS landscape, such as
     * an EC2 client, a Route53 client, etc.
     */
    static <ShardingKey, MetricsT extends ApplicationProcessMetrics> AwsLandscape<ShardingKey, MetricsT> obtain() {
        return new AwsLandscapeImpl<>();
    }
    
    /**
     * Launches a new {@link Host} from a given image into the availability zone specified and controls network access
     * to that instance by setting the security groups specified for the resulting host.
     * 
     * @param keyName
     *            the SSH key pair name to use when launching; this will grant root access with the corresponding
     *            private key; see also {@link #getKeyPairInfo(Region, String)}
     */
    default AwsInstance launchHost(MachineImage<AwsInstance> fromImage, AvailabilityZone az, String keyName, Iterable<SecurityGroup> securityGroups) {
        return launchHosts(1, fromImage, az, keyName, securityGroups).iterator().next();
    }

    /**
     * Launches a number of new {@link Host}s from a given image into the availability zone specified and controls
     * network access to that instance by setting the security groups specified for the resulting host.
     * 
     * @param keyName
     *            the SSH key pair name to use when launching; this will grant root access with the corresponding
     *            private key; see also {@link #getKeyPairInfo(Region, String)}
     */
    Iterable<AwsInstance> launchHosts(int numberOfHostsToLaunch, MachineImage<AwsInstance> fromImage, AvailabilityZone az,
            String keyName, Iterable<SecurityGroup> securityGroups);
    
    AmazonMachineImage getImage(Region region, String imageId);
    
    KeyPairInfo getKeyPairInfo(Region region, String keyName);
    
    void deleteKeyPair(Region region, String keyName);
    
    /**
     * Uploads the public key to AWS under the name "keyName", stores it in this landscape and returns the key pair ID
     */
    String importKeyPair(Region region, byte[] publicKey, byte[] unencryptedPrivateKey, String keyName) throws JSchException;

    void terminate(AwsInstance host);

    SSHKeyPair getSSHKeyPair(Region region, String keyName);
    
    byte[] getDescryptedPrivateKey(SSHKeyPair keyPair) throws JSchException;

    void addSSHKeyPair(SSHKeyPair keyPair);

    /**
     * Creates a key pair with the given name in the region specified and obtains the key details and stores them in
     * this landscape persistently, such that {@link #getKeyPairInfo(Region, String)} as well as
     * {@link #getSSHKeyPair(Region, String)} will be able to obtain (information on) the key.
     * 
     * @return the key ID as string, usually starting with the prefix "key-"
     */
    SSHKeyPair createKeyPair(Region region, String keyName) throws JSchException;

    Instance getInstance(String instanceId, Region region);

    /**
     * @param hostname the fully-qualified host name
     */
    ChangeInfo setDNSRecordToHost(String hostedZoneId, String hostname, Host host);

    /**
     * @param hostname the fully-qualified host name
     */
    ChangeInfo setDNSRecordToApplicationLoadBalancer(String hostedZoneId, String hostname, ApplicationLoadBalancer alb);

    String getDefaultDNSHostedZoneId();

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
}
