package com.sap.sse.landscape.aws.orchestration;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.OutboundReplicationConfiguration;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.UserDataProvider;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.orchestration.StartHost;
import com.sap.sse.landscape.rabbitmq.RabbitMQEndpoint;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Launches an AWS host when {@link #run() run}. The resulting {@link AwsInstance} can then be obtained by
 * calling {@link #getHost()}.
 * 
 * @author Axel Uhl (D043530)
 */
public abstract class StartAwsHost<ShardingKey,
                          MetricsT extends ApplicationProcessMetrics,
                          MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                          ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                          HostT extends AwsInstance<ShardingKey, MetricsT>>
extends StartHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
    /**
     * The {@link AwsLandscape#getLatestImageWithTag(com.sap.sse.landscape.Region, String, String)} method is
     * used to obtain default images for specific AWS host starting procedures that subclass this class. The
     * Amazon Machine Images (AMIs) for this are then expected to be tagged with a tag named as specified by this
     * constant ("image-type"). The tag value then must match what the subclass wants.
     * 
     * @see #getLatestImageOfType(String)
     */
    private final static String IMAGE_TYPE_TAG_NAME = "image-type";

    private static final String NAME_TAG_NAME = "Name";

    private final List<String> userData;
    private final InstanceType instanceType;
    private final AwsAvailabilityZone availabilityZone;
    private final String keyName;
    private final Iterable<SecurityGroup> securityGroups;
    private final Optional<Tags> tags;
    private final HostSupplier<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> hostSupplier;
    private HostT host;

    
    /**
     * A builder that helps building an instance of type {@link StartAwsHost} or any subclass thereof (then using
     * specialized builders). The following default rules apply:
     * <ul>
     * <li>If an {@link #getImageType() image type} has been specified, it serves as the default for looking up the
     * {@link #getMachineImage() image} to launch from. However, an image type set explicitly, or subclasses overriding
     * {@link #getMachineImage()} may take precedence.</li>
     * <li>If no {@link #setRelease(Release) release is set}, an empty {@link Optional} will be returned by
     * {@link #getRelease()}, indicating to use the default release pre-deployed in the image launched.</li>
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
     * <li>If no {@link #setDatabaseName(String) database name is set explicitly}, it defaults to the
     * {@link #getServerName() server name}.</li>
     * <li>The {@link #getOptionalTimeout() optional timeout} defaults to an {@link Optional#empty() empty optional},
     * meaning that waiting for the instance won't timeout by default.</li>
     * <li>The {@link #getOutboundReplicationConfiguration() output replication}
     * {@link OutboundReplicationConfiguration#getOutboundReplicationExchangeName() exchange name} defaults to the
     * {@link #getServerName() server name}.</li>
     * <li>The {@link #getOutboundReplicationConfiguration() output replication}
     * {@link OutboundReplicationConfiguration#getOutboundRabbitMQEndpoint() RabbitMQ endpoint} defaults to the
     * {@link #getInboundReplicationConfiguration() inbound}
     * {@link InboundReplicationConfiguration#getInboundRabbitMQEndpoint() RabbitMQ endpoint}.</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<T extends StartAwsHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends StartHost.Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        @Override
        AmazonMachineImage<ShardingKey, MetricsT> getMachineImage();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setMachineImage(AmazonMachineImage<ShardingKey, MetricsT> machineImage);
        
        /**
         * When not {@code null}, the newest {@link AmazonMachineImage} tagged with a tag named as specified by the constant {@link #IMAGE_TYPE_TAG_NAME}
         * with the value provided by the result of this method will be searched and will be used as the default for {@link #getMachineImage()}.
         */
        String getImageType();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>  setImageType(String imageType);

        /**
         * By default, the release pre-deployed in the image will be used, represented by an empty {@link Optional}
         * returned by this default method implementation.
         */
        Optional<Release> getRelease();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setRelease(Optional<Release> release);

        @Override
        AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>  setLandscape(AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape);

        InstanceType getInstanceType();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setInstanceType(InstanceType instanceType);

        AwsAvailabilityZone getAvailabilityZone();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setAvailabilityZone(AwsAvailabilityZone availabilityZone);

        String getKeyName();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setKeyName(String keyName);

        Iterable<SecurityGroup> getSecurityGroups();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setSecurityGroups(Iterable<SecurityGroup> securityGroups);

        Optional<Tags> getTags();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setTags(Optional<Tags> tags);

        Iterable<String> getUserData();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setUserData(String[] userData);

        AwsRegion getRegion();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setRegion(AwsRegion region);
        
        String getInstanceName();

        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setInstanceName(String name);
        
        String getServerName();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setServerName(String serverName);

        String getDatabaseName();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setDatabaseName(String databaseName);
        
        Optional<InboundReplicationConfiguration> getInboundReplicationConfiguration();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setReplicationConfiguration(
                InboundReplicationConfiguration replicationConfiguration);

        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setOutboundReplicationConfiguration(OutboundReplicationConfiguration outboundReplicationConfiguration);

        RabbitMQEndpoint getRabbitConfiguration();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setRabbitConfiguration(RabbitMQEndpoint rabbitConfiguration);

        Database getDatabaseConfiguration();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setDatabaseConfiguration(Database databaseConfiguration);
        
        String getCommaSeparatedEmailAddressesToNotifyOfStartup();

        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setCommaSeparatedEmailAddressesToNotifyOfStartup(
                String commaSeparatedEmailAddressesToNotifyOfStartup);
        
        /**
         * A timeout for interacting with the instance, such as when creating an SSH / SFTP connection or waiting for its
         * public IP address.
         */
        Optional<Duration> getOptionalTimeout();
        
        Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setOptionalTimeout(Optional<Duration> optionalTimeout);
        
        HostSupplier<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> getHostSupplier();

        OutboundReplicationConfiguration getOutboundReplicationConfiguration();
    }
    
    protected abstract static class BuilderImpl<T extends StartAwsHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>> implements Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        private AmazonMachineImage<ShardingKey, MetricsT> machineImage;
        private String imageType;
        private Optional<Release> release = Optional.empty();
        private AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape;
        private InstanceType instanceType;
        private AwsAvailabilityZone availabilityZone;
        private String keyName;
        private Iterable<SecurityGroup> securityGroups;
        private Optional<Tags> tags = Optional.empty();
        private List<String> userData = new ArrayList<>();
        private AwsRegion region;
        private String instanceName;
        private String serverName;
        private String databaseName;
        private Database databaseConfiguration;
        private RabbitMQEndpoint rabbitConfiguration;
        private Optional<InboundReplicationConfiguration> inboundReplicationConfiguration = Optional.empty();
        private OutboundReplicationConfiguration outboundReplicationConfiguration;
        private String commaSeparatedEmailAddressesToNotifyOfStartup;
        private Optional<Duration> optionalTimeout;
        
        @Override
        public AmazonMachineImage<ShardingKey, MetricsT> getMachineImage() {
            return machineImage == null ? getLatestImageOfType(getImageType(), getLandscape(), getRegion()) : machineImage;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setMachineImage(
                AmazonMachineImage<ShardingKey, MetricsT> machineImage) {
            this.machineImage = machineImage;
            return this;
        }

        @Override
        public String getImageType() {
            return imageType;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setImageType(
                String imageType) {
            this.imageType = imageType;
            return this;
        }

        @Override
        public Optional<Release> getRelease() {
            return release;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setRelease(Optional<Release> release) {
            this.release = release;
            return this;
        }

        @Override
        public AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
            return landscape;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setLandscape(
                AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
            this.landscape = landscape;
            return this;
        }

        @Override
        public InstanceType getInstanceType() {
            return instanceType;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setInstanceType(
                InstanceType instanceType) {
            this.instanceType = instanceType;
            return this;
        }

        @Override
        public AwsAvailabilityZone getAvailabilityZone() {
            return availabilityZone == null ? getRandomAvailabilityZone(getRegion(), getLandscape()) : availabilityZone;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setAvailabilityZone(
                AwsAvailabilityZone availabilityZone) {
            this.availabilityZone = availabilityZone;
            return this;
        }

        @Override
        public String getKeyName() {
            return keyName;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setKeyName(String keyName) {
            this.keyName = keyName;
            return this;
        }

        @Override
        public Iterable<SecurityGroup> getSecurityGroups() {
            return securityGroups == null ? Collections.singleton(getLandscape().getDefaultSecurityGroupForApplicationHosts(getRegion())) : securityGroups;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setSecurityGroups(
                Iterable<SecurityGroup> securityGroups) {
            this.securityGroups = securityGroups;
            return this;
        }

        @Override
        public Optional<Tags> getTags() {
            return tags;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setTags(Optional<Tags> tags) {
            this.tags = tags;
            return this;
        }

        @Override
        public Iterable<String> getUserData() {
            return Collections.unmodifiableList(userData);
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setUserData(
                String[] userData) {
            this.userData.clear();
            for (final String userDataElement : userData) {
                this.userData.add(userDataElement);
            }
            return this;
        }

        @Override
        public AwsRegion getRegion() {
            return region == null ? getAvailabilityZone().getRegion() : region;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setRegion(AwsRegion region) {
            this.region = region;
            return this;
        }
        
        @Override
        public String getInstanceName() {
            return instanceName;
        }
        
        protected boolean isInstanceNameSet() {
            return instanceName != null;
        }
        
        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setInstanceName(String instanceName) {
            this.instanceName = instanceName;
            return this;
        }

        @Override
        public String getServerName() {
            return serverName;
        }
        
        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setServerName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        @Override
        public String getDatabaseName() {
            return databaseName == null ? getServerName() : databaseName;
        }
        
        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        @Override
        public Database getDatabaseConfiguration() {
            return databaseConfiguration == null ? getLandscape().getDatabase(getRegion(), getDatabaseName()) : databaseConfiguration;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setDatabaseConfiguration(Database databaseConfiguration) {
            this.databaseConfiguration = databaseConfiguration;
            return this;
        }

        @Override
        public RabbitMQEndpoint getRabbitConfiguration() {
            return rabbitConfiguration;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setRabbitConfiguration(RabbitMQEndpoint rabbitConfiguration) {
            this.rabbitConfiguration = rabbitConfiguration;
            return this;
        }
        
        protected boolean isOutboundReplicationExchangeNameSet() {
            return outboundReplicationConfiguration != null && outboundReplicationConfiguration.getOutboundReplicationExchangeName() != null;
        }
        
        protected boolean isInboundReplicationRabbitMQEndpointSet() {
            return inboundReplicationConfiguration != null && inboundReplicationConfiguration.isPresent()
                    && inboundReplicationConfiguration.get().getInboundRabbitMQEndpoint() != null;
        }
        
        protected boolean isOutboundReplicationRabbitMQEndpointSet() {
            return outboundReplicationConfiguration != null && outboundReplicationConfiguration.getOutboundRabbitMQEndpoint() != null;
        }
        
        @Override
        public OutboundReplicationConfiguration getOutboundReplicationConfiguration() {
            final OutboundReplicationConfiguration.Builder resultBuilder;
            if (outboundReplicationConfiguration != null) {
                resultBuilder = OutboundReplicationConfiguration.copy(outboundReplicationConfiguration);
            } else {
                resultBuilder = OutboundReplicationConfiguration.builder();
            }
            if (!isOutboundReplicationExchangeNameSet()) {
                resultBuilder.setOutboundReplicationExchangeName(getServerName());
            }
            if (!isOutboundReplicationRabbitMQEndpointSet()) {
                getInboundReplicationConfiguration().ifPresent(irc->resultBuilder.setOutboundRabbitMQEndpoint(irc.getInboundRabbitMQEndpoint()));
            }
            return resultBuilder.build();
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setOutboundReplicationConfiguration(OutboundReplicationConfiguration outboundReplicationConfiguration) {
            this.outboundReplicationConfiguration = outboundReplicationConfiguration;
            return this;
        }
        
        @Override
        public Optional<InboundReplicationConfiguration> getInboundReplicationConfiguration() {
            final InboundReplicationConfiguration.Builder resultBuilder;
            if (inboundReplicationConfiguration == null || !inboundReplicationConfiguration.isPresent()) {
                resultBuilder = InboundReplicationConfiguration.builder();
            } else {
                resultBuilder = InboundReplicationConfiguration.copy(inboundReplicationConfiguration.get());
            }
            return !isInboundReplicationRabbitMQEndpointSet()
                    ? Optional.of(resultBuilder
                            .setInboundRabbitMQEndpoint(getLandscape().getDefaultRabbitConfiguration(getRegion()))
                            .build())
                    : inboundReplicationConfiguration;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setReplicationConfiguration(InboundReplicationConfiguration replicationConfiguration) {
            this.inboundReplicationConfiguration = Optional.of(replicationConfiguration);
            return this;
        }
        
        @Override
        public String getCommaSeparatedEmailAddressesToNotifyOfStartup() {
            return commaSeparatedEmailAddressesToNotifyOfStartup;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setCommaSeparatedEmailAddressesToNotifyOfStartup(String commaSeparatedEmailAddressesToNotifyOfStartup) {
            this.commaSeparatedEmailAddressesToNotifyOfStartup = commaSeparatedEmailAddressesToNotifyOfStartup;
            return this;
        }

        @Override
        public Optional<Duration> getOptionalTimeout() {
            return optionalTimeout == null ? Optional.empty() : optionalTimeout;
        }

        @Override
        public Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setOptionalTimeout(
                Optional<Duration> optionalTimeout) {
            this.optionalTimeout = optionalTimeout;
            return this;
        }
    }
    
    protected StartAwsHost(Builder<? extends StartAwsHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder) {
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
        builder.getRelease().ifPresent(this::addUserData);
        addUserData(builder.getDatabaseConfiguration());
        addUserData(builder.getOutboundReplicationConfiguration());
        addUserData(ProcessConfigurationVariable.SERVER_NAME, builder.getServerName());
        addUserData(ProcessConfigurationVariable.SERVER_STARTUP_NOTIFY, builder.getCommaSeparatedEmailAddressesToNotifyOfStartup());
        builder.getInboundReplicationConfiguration().ifPresent(this::addUserData);
    }
    
    protected static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    AmazonMachineImage<ShardingKey, MetricsT> getLatestImageOfType(String imageType, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, Region region) {
        return landscape.getLatestImageWithTag(region, IMAGE_TYPE_TAG_NAME, imageType);
    }
    
    protected static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    AwsAvailabilityZone getRandomAvailabilityZone(AwsRegion region, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        final Iterable<AvailabilityZone> azs = landscape.getAvailabilityZones(region);
        return (AwsAvailabilityZone) Util.get(azs, new Random().nextInt(Util.size(azs)));
    }

    protected static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    Set<SecurityGroup> getDefaultSecurityGroupForApplicationHosts(Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, Region region) {
        return Collections.singleton(landscape.getDefaultSecurityGroupForApplicationHosts(region));
    }

    @Override
    public AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        return (AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>) super.getLandscape();
    }

    @Override
    public void run() throws Exception {
        HostT castHost = getLandscape().launchHost(hostSupplier,
            getMachineImage(), getInstanceType(), getAvailabilityZone(), getKeyName(), getSecurityGroups(), getTags(),
                Util.toArray(getUserData(), new String[0]));
        host = castHost;
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

    private String getKeyName() {
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
     * Appends {@code moreUserData} to the end of {@link #userData}
     */
    protected void addUserData(Iterable<String> moreUserData) {
        Util.addAll(moreUserData, userData);
    }
    
    /**
     * @param value an unquoted string; it will be mapped to the right hand side of a Bash variable assignment and for 
     * that purpose will be enclosed in double-quotes ({@code "}), and double-quote characters in the {@code value} string
     * will be escaped by preceding them with a {@code \} (backslash) character.
     */
    protected void addUserData(ProcessConfigurationVariable userDataVariable, String value) {
        userData.add(userDataVariable.name()+"=\""+value.replaceAll("\"", "\\\"")+"\"");
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
}
