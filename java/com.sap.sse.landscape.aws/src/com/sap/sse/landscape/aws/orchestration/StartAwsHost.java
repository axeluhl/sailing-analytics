package com.sap.sse.landscape.aws.orchestration;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.UserDataProvider;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.orchestration.StartHost;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Launches an AWS host when {@link #run() run}. The resulting {@link AwsInstance} can then be obtained by
 * calling {@link #getHost()}.
 * 
 * @author Axel Uhl (D043530)
 */
public abstract class StartAwsHost<ShardingKey, HostT extends AwsInstance<ShardingKey>>
extends StartHost<ShardingKey, HostT> {
    public static final String NAME_TAG_NAME = "Name";

    private final List<String> userData;
    private final InstanceType instanceType;
    private final AwsAvailabilityZone availabilityZone;
    private final String keyName;
    private final Iterable<SecurityGroup> securityGroups;
    private final Optional<Tags> tags;
    private final HostSupplier<ShardingKey, HostT> hostSupplier;
    private final byte[] privateKeyEncryptionPassphrase;
    private HostT host;
    
    /**
     * A builder that helps building an instance of type {@link StartAwsHost} or any subclass thereof (then using
     * specialized builders). The following default rules apply:
     * <ul>
     * <li>If an {@link #getImageType() image type} has been specified, it serves as the default for looking up the
     * {@link #getMachineImage() image} to launch from. However, an image type set explicitly, or subclasses overriding
     * {@link #getMachineImage()} may take precedence.</li>
     * <li>If no {@link #setAvailabilityZone(AwsAvailabilityZone) availability zone} is specified, this builder will try
     * to obtain the {@link #getRegion()} which in this case must have been {@link #setRegion(AwsRegion)} or otherwise
     * be returned from an overridden {@link #getRegion()} in a specialized builder, and will
     * {@link StartAwsHost#getRandomAvailabilityZone(AwsRegion, AwsLandscape) pick an availability zone randomly} within
     * that region.</li>
     * <li>Conversely, if an {@link #setAvailabilityZone(AwsAvailabilityZone) availability zone has been set}, its
     * {@link AwsAvailabilityZone#getRegion() region} will be the default answer of {@link #getRegion()}.</li>
     * <li>If no {@link #setSecurityGroups(Iterable) security group} has been set, the {@link #getLandscape() landscape}
     * is asked to provide its {@link AwsLandscape#getDefaultSecurityGroupForApplicationHosts(Region) default security
     * group for application hosts} in the {@link #getRegion() region} used by this builder.</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, HostT>,
    T extends StartAwsHost<ShardingKey, HostT>, ShardingKey,
    HostT extends AwsInstance<ShardingKey>>
    extends StartHost.Builder<BuilderT, T, ShardingKey, HostT> {
        BuilderT setInstanceType(InstanceType instanceType);
        
        BuilderT setAvailabilityZone(AwsAvailabilityZone availabilityZone);

        /**
         * Tells the name of the SSH key stored in the AWS landscape that is to be used to launch the instance; the
         * respective public key will be added so that an owner of the corresponding private key can
         * {@link Host#createSshChannel(String, Optional, byte[]) SSH} into the resulting host.<p>
         * 
         * Make sure to also provide the {@link #setPrivateKeyEncryptionPassphrase(byte[]) private key pass phrase}
         * that is required to decrypt the pass phrase of the private key belonging to the public key identified
         * by {@code keyName}. This private key is expected to be stored together with the entire key pair in the
         * "landscape" persistently.
         */
        BuilderT setKeyName(String keyName);
        
        BuilderT setPrivateKeyEncryptionPassphrase(byte[] privateKeyEncryptionPassphrase);

        BuilderT setSecurityGroups(Iterable<SecurityGroup> securityGroups);
        
        BuilderT setTags(Tags tags);

        BuilderT setUserData(String[] userData);

        BuilderT setRegion(AwsRegion region);
        
        BuilderT setInstanceName(String name);
        
        BuilderT setHostSupplier(HostSupplier<ShardingKey, HostT> hostSupplier);

        AmazonMachineImage<ShardingKey> getMachineImage();
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, HostT>,
    T extends StartAwsHost<ShardingKey, HostT>, ShardingKey,
    HostT extends AwsInstance<ShardingKey>>
    extends StartHost.BuilderImpl<BuilderT, T, ShardingKey, HostT>
    implements Builder<BuilderT, T, ShardingKey, HostT> {
        private InstanceType instanceType;
        private AwsAvailabilityZone availabilityZone;
        private String keyName;
        private Iterable<SecurityGroup> securityGroups;
        private Optional<Tags> tags = Optional.empty();
        private List<String> userData = new ArrayList<>();
        private AwsRegion region;
        private String instanceName;
        private HostSupplier<ShardingKey, HostT> hostSupplier;
        private byte[] privateKeyEncryptionPassphrase;
        
        protected AwsLandscape<ShardingKey> getLandscape() {
            return (AwsLandscape<ShardingKey>) super.getLandscape();
        }

        protected InstanceType getInstanceType() {
            return instanceType;
        }

        @Override
        public BuilderT setInstanceType(InstanceType instanceType) {
            this.instanceType = instanceType;
            return self();
        }

        protected AwsAvailabilityZone getAvailabilityZone() {
            return availabilityZone == null ? getRandomAvailabilityZone(getRegion(), getLandscape()) : availabilityZone;
        }

        @Override
        public BuilderT setAvailabilityZone(
                AwsAvailabilityZone availabilityZone) {
            this.availabilityZone = availabilityZone;
            return self();
        }

        protected String getKeyName() {
            return keyName;
        }

        @Override
        public BuilderT setKeyName(String keyName) {
            this.keyName = keyName;
            return self();
        }

        protected Iterable<SecurityGroup> getSecurityGroups() {
            return securityGroups == null ? Collections.singleton(getLandscape().getDefaultSecurityGroupForApplicationHosts(getRegion())) : securityGroups;
        }
        
        protected boolean isSecurityGroupsSet() {
            return securityGroups != null;
        }

        @Override
        public BuilderT setSecurityGroups(
                Iterable<SecurityGroup> securityGroups) {
            this.securityGroups = securityGroups;
            return self();
        }

        protected Optional<Tags> getTags() {
            return tags;
        }

        @Override
        public BuilderT setTags(Tags tags) {
            this.tags = Optional.ofNullable(tags);
            return self();
        }

        protected Iterable<String> getUserData() {
            return Collections.unmodifiableList(userData);
        }

        @Override
        public BuilderT setUserData(
                String[] userData) {
            this.userData.clear();
            for (final String userDataElement : userData) {
                this.userData.add(userDataElement);
            }
            return self();
        }

        protected AwsRegion getRegion() {
            return region == null
                    ? availabilityZone == null
                        ? getLandscape().getDefaultRegion()
                        : getAvailabilityZone().getRegion()
                    : region;
        }

        @Override
        public BuilderT setRegion(AwsRegion region) {
            this.region = region;
            return self();
        }
        
        protected String getInstanceName() {
            return instanceName;
        }
        
        protected boolean isInstanceNameSet() {
            return instanceName != null;
        }
        
        @Override
        public BuilderT setInstanceName(String instanceName) {
            this.instanceName = instanceName;
            return self();
        }

        protected HostSupplier<ShardingKey, HostT> getHostSupplier() {
            return hostSupplier;
        }
        
        @Override
        public BuilderT setHostSupplier(HostSupplier<ShardingKey, HostT> hostSupplier) {
            this.hostSupplier = hostSupplier;
            return self();
        }
        
        @Override
        public BuilderT setPrivateKeyEncryptionPassphrase(byte[] privateKeyEncryptionPassphrase) {
            this.privateKeyEncryptionPassphrase = privateKeyEncryptionPassphrase;
            return self();
        }
        
        protected byte[] getPrivateKeyEncryptionPassphrase() {
            return privateKeyEncryptionPassphrase;
        }
        
        @Override
        public AmazonMachineImage<ShardingKey> getMachineImage() {
            @SuppressWarnings("unchecked")
            final AmazonMachineImage<ShardingKey> result = (AmazonMachineImage<ShardingKey>) super.getMachineImage();
            return result;
        }
    }
    
    protected StartAwsHost(BuilderImpl<?, ? extends StartAwsHost<ShardingKey, HostT>, ShardingKey, HostT> builder) {
        super(builder);
        this.userData = new ArrayList<>();
        for (final String ud : builder.getUserData()) {
            this.userData.add(ud);
        }
        this.instanceType = builder.getInstanceType();
        this.availabilityZone = builder.getAvailabilityZone();
        this.keyName = builder.getKeyName();
        this.tags = Optional.of(builder.getTags().orElse(Tags.empty()).and(NAME_TAG_NAME, builder.getInstanceName()));
        this.securityGroups = builder.getSecurityGroups();
        this.hostSupplier = builder.getHostSupplier();
        this.privateKeyEncryptionPassphrase = builder.getPrivateKeyEncryptionPassphrase();
    }
    
    protected static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    AmazonMachineImage<ShardingKey> getLatestImageOfType(String imageType, AwsLandscape<ShardingKey> landscape, Region region) {
        return landscape.getLatestImageWithType(region, imageType);
    }
    
    protected static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    AwsAvailabilityZone getRandomAvailabilityZone(AwsRegion region, AwsLandscape<ShardingKey> landscape) {
        final Iterable<AwsAvailabilityZone> azs = landscape.getAvailabilityZones(region);
        return (AwsAvailabilityZone) Util.get(azs, new Random().nextInt(Util.size(azs)));
    }

    protected static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    Set<SecurityGroup> getDefaultSecurityGroupForApplicationHosts(Landscape<ShardingKey> landscape, Region region) {
        return Collections.singleton(landscape.getDefaultSecurityGroupForApplicationHosts(region));
    }

    @Override
    public AwsLandscape<ShardingKey> getLandscape() {
        return (AwsLandscape<ShardingKey>) super.getLandscape();
    }

    @Override
    public void run() throws Exception {
        host = getLandscape().launchHost(hostSupplier,
            getMachineImage(), getInstanceType(), getAvailabilityZone(), getKeyName(), getSecurityGroups(), getTags(),
                Util.toArray(getUserData(), new String[0]));
    }
    
    /**
     * @return {@code null} before {@link #run()} is called; the host launched afterwards
     */
    public HostT getHost() {
        return host;
    }

    private Optional<Tags> getTags() {
        return tags;
    }

    private Iterable<SecurityGroup> getSecurityGroups() {
        return securityGroups;
    }

    protected String getKeyName() {
        return keyName;
    }

    private AwsAvailabilityZone getAvailabilityZone() {
        return availabilityZone;
    }

    private InstanceType getInstanceType() {
        return instanceType;
    }

    /**
     * @return the user data to pass to EC2 when launching the instance; based on the {@link #userData} passed to this
     *         object's constructor plus extensions added by calling {@link #addUserData}. Subclasses may override this
     *         method to change or add to these user data.
     */
    protected Iterable<String> getUserData() throws URISyntaxException {
        return userData;
    }
    
    /**
     * Appends {@code moreUserData} to the end of {@link #userData} without any quoting or escaping
     */
    protected void addUserData(Iterable<String> moreUserData) {
        Util.addAll(moreUserData, userData);
    }
    
    /**
     * Appends a line of user data in the form of an environment variable assignment; the {@code value} will be
     * quoted and escaped.
     * 
     * @param value
     *            an unquoted string; it will be mapped to the right hand side of a Bash variable assignment and for
     *            that purpose will be enclosed in double-quotes ({@code "}), and double-quote and single-quote and backslash
     *            characters in the {@code value} string will be escaped by preceding them with a {@code \} (backslash)
     *            character.
     */
    protected void addUserData(ProcessConfigurationVariable userDataVariable, String value) {
        userData.add(UserDataProvider.getAsEnvironmentVariableAssignment(userDataVariable, value));
    }
    
    /**
     * Appends {@code moreUserData} to the end of {@link #userData}
     */
    protected void addUserData(UserDataProvider userDataProvider) {
        if (userDataProvider != null) {
            for (final Entry<ProcessConfigurationVariable, String> userDataEntry : userDataProvider.getUserData().entrySet()) {
                addUserData(userDataEntry.getKey(), userDataEntry.getValue());
            }
        }
    }
    
    protected byte[] getPrivateKeyEncryptionPassphrase() {
        return privateKeyEncryptionPassphrase;
    }
}
