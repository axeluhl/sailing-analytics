package com.sap.sse.landscape.aws.orchestration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.OutboundReplicationConfiguration;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.UserDataProvider;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.mongodb.Database;

/**
 * Configures an application server process by collecting, defaulting and then providing {@link #getUserData() "user
 * data"} which generally consists of a set of environment variable assignments which can be evaluated, inserted into an
 * {@code env.sh} file, either directly by a procedure remotely, or by setting these variable definitions as the "user
 * data" during the launch of an AWS instance from where they are passed on to an initialization script running during
 * the instance's boot sequence and using these user data to configure an initial application process deployed to that
 * instance.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AwsApplicationConfiguration<ShardingKey,
MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
implements UserDataProvider {
    /**
     * A builder that helps building an instance of type {@link AwsApplicationConfiguration} or any subclass thereof (then
     * using specialized builders). The following default rules apply, in addition to the defaults rules of the builders
     * that this builder interface {@link StartAwsHost.Builder extends}.
     * <ul>
     * <li>If no {@link #setRelease(Release) release is set}, an empty {@link Optional} will be returned by
     * {@link #getRelease()}, indicating to use the default release pre-deployed in the image launched.</li>
     * <li>If no {@link #setDatabaseName(String) database name is set explicitly}, it defaults to the
     * {@link #getServerName() server name}.</li>
     * <li>The {@link #setInboundReplicationConfiguration(InboundReplicationConfiguration) inbound replication}
     * {@link InboundReplicationConfiguration#getInboundRabbitMQEndpoint() RabbitMQ endpoint} defaults to the region's
     * {@link AwsLandscape#getDefaultRabbitConfiguration(com.sap.sse.landscape.aws.impl.AwsRegion) default RabbitMQ
     * configuration}. Note that this setting will take effect only if auto-replication is activated for one or more
     * replicables (see {@link InboundReplicationConfiguration#getReplicableIds()}).</li>
     * <li>The {@link #setOutboundReplicationConfiguration() outbound replication}
     * {@link OutboundReplicationConfiguration#getOutboundReplicationExchangeName() exchange name} defaults to the
     * {@link #getServerName() server name}.</li>
     * <li>The {@link #setOutboundReplicationConfiguration() outbound replication}
     * {@link OutboundReplicationConfiguration#getOutboundRabbitMQEndpoint() RabbitMQ endpoint} defaults to the
     * {@link #getInboundReplicationConfiguration() inbound}
     * {@link InboundReplicationConfiguration#getInboundRabbitMQEndpoint() RabbitMQ endpoint}.</li>
     * <li>The {@link #setMailFrom(String) mail From: address} defaults to {@code noreply@sapsailing.com}.</li>
     * <li>The {@link #setMailSmtpPort(int) SMTP port} defaults to 25.</li>
     * <li>The {@link #setMailSmtpHost(String) mail SMTP host} defaults to <tt>email-smtp.${region}.amazonaws.com</tt>.</li>
     * <li>The {@link #setMailSmtpAuth(boolean) mail SMTP authentication} is activated by default.</li>
     * <li>The {@link #setMailSmtpUser(String) mail SMTP user} defaults to {@code AKIAIHCQEFAZDLIK7SUQ} which is the
     * project's AWS SES (Simple e-Mail Service) username. The {@link #setMailSmtpPassword(String) password}, however,
     * must explicitly be provided and does not default to any non-{@code null}, non-empty value.</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>,
    T extends AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends com.sap.sse.common.Builder<BuilderT, T> {
        BuilderT setRegion(AwsRegion region);
        
        BuilderT setLandscape(AwsLandscape<ShardingKey> landscape);
        
        BuilderT setRelease(Release release);

        BuilderT setServerName(String serverName);
        
        BuilderT setDatabaseName(String databaseName);
        
        BuilderT setInboundReplicationConfiguration(InboundReplicationConfiguration replicationConfiguration);

        BuilderT setOutboundReplicationConfiguration(OutboundReplicationConfiguration outboundReplicationConfiguration);

        BuilderT setDatabaseConfiguration(Database databaseConfiguration);
        
        BuilderT setCommaSeparatedEmailAddressesToNotifyOfStartup(String commaSeparatedEmailAddressesToNotifyOfStartup);
        
        BuilderT setMailFrom(String mailFrom);
        
        BuilderT setMailSmtpHost(String mailSmtpHost);
        
        BuilderT setMailSmtpPort(int mailSmtpPort);
        
        BuilderT setMailSmtpAuth(boolean mailSmtpAuth);
        
        BuilderT setMailSmtpUser(String mailSmtpUser);
        
        BuilderT setMailSmtpPassword(String mailSmtpPassword);
    }
    
    /**
     * The builder needs to know the {@link AwsRegion} in which the application will be run. In this region, discovery
     * of default database and messaging endpoints is performed.
     */
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>,
    T extends AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    implements Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
        /**
         * The pattern requires the region ID as {@link String} parameter and produces an AWS SES SMTP hostname for that region.
         */
        private static final String DEFAULT_SMTP_HOSTNAME_PATTERN = "email-smtp.%s.amazonaws.com";
        private static final String DEFAULT_SMTP_USER = "AKIAIHCQEFAZDLIK7SUQ";
        private static final Integer DEFAULT_SMTP_PORT = 25;
        private AwsLandscape<ShardingKey> landscape;
        private AwsRegion region;
        private Optional<Release> release = Optional.empty();
        private String serverName;
        private String databaseName;
        private Database databaseConfiguration;
        private Optional<InboundReplicationConfiguration> inboundReplicationConfiguration = Optional.empty();
        private OutboundReplicationConfiguration outboundReplicationConfiguration;
        private String commaSeparatedEmailAddressesToNotifyOfStartup;
        private String mailFrom;
        private String mailSmtpHost;
        private Integer mailSmtpPort;
        private Boolean mailSmtpAuth;
        private String mailSmtpUser;
        private String mailSmtpPassword;

        @Override
        public BuilderT setRegion(AwsRegion region) {
            this.region = region;
            return self();
        }
        
        protected AwsRegion getRegion() {
            return region;
        }
        
        @Override
        public BuilderT setLandscape(AwsLandscape<ShardingKey> landscape) {
            this.landscape = landscape;
            return self();
        }
        
        protected AwsLandscape<ShardingKey> getLandscape() {
            return landscape;
        }

        /**
         * By default, the release pre-deployed in the image will be used, represented by an empty {@link Optional}
         * returned by this default method implementation.
         */
        protected Optional<Release> getRelease() {
            return release;
        }

        @Override
        public BuilderT setRelease(Release release) {
            this.release = Optional.ofNullable(release);
            return self();
        }

        protected String getServerName() {
            return serverName;
        }
        
        @Override
        public BuilderT setServerName(String serverName) {
            this.serverName = serverName;
            return self();
        }

        protected String getDatabaseName() {
            return databaseName == null ? getServerName() : databaseName;
        }
        
        @Override
        public BuilderT setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return self();
        }

        protected Database getDatabaseConfiguration() {
            return databaseConfiguration == null ? getLandscape().getDatabase(getRegion(), getDatabaseName()) : databaseConfiguration;
        }

        @Override
        public BuilderT setDatabaseConfiguration(Database databaseConfiguration) {
            this.databaseConfiguration = databaseConfiguration;
            return self();
        }

        protected boolean isOutboundReplicationExchangeNameSet() {
            return outboundReplicationConfiguration != null && outboundReplicationConfiguration.getOutboundReplicationExchangeName() != null;
        }
        
        protected boolean isInboundReplicationRabbitMQEndpointSet() {
            return inboundReplicationConfiguration != null && inboundReplicationConfiguration.isPresent()
                    && inboundReplicationConfiguration.get().getInboundRabbitMQEndpoint() != null;
        }
        
        protected boolean isInboundReplicationExchangeNameSet() {
            return inboundReplicationConfiguration != null && inboundReplicationConfiguration.isPresent()
                    && inboundReplicationConfiguration.get().getInboundMasterExchangeName() != null;
        }
        
        protected boolean isOutboundReplicationRabbitMQEndpointSet() {
            return outboundReplicationConfiguration != null && outboundReplicationConfiguration.getOutboundRabbitMQEndpoint() != null;
        }
        
        protected OutboundReplicationConfiguration getOutboundReplicationConfiguration() {
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
        public BuilderT setOutboundReplicationConfiguration(OutboundReplicationConfiguration outboundReplicationConfiguration) {
            this.outboundReplicationConfiguration = outboundReplicationConfiguration;
            return self();
        }
        
        protected Optional<InboundReplicationConfiguration> getInboundReplicationConfiguration() {
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
        public BuilderT setInboundReplicationConfiguration(InboundReplicationConfiguration replicationConfiguration) {
            this.inboundReplicationConfiguration = Optional.of(replicationConfiguration);
            return self();
        }
        
        protected String getCommaSeparatedEmailAddressesToNotifyOfStartup() {
            return commaSeparatedEmailAddressesToNotifyOfStartup;
        }

        @Override
        public BuilderT setCommaSeparatedEmailAddressesToNotifyOfStartup(String commaSeparatedEmailAddressesToNotifyOfStartup) {
            this.commaSeparatedEmailAddressesToNotifyOfStartup = commaSeparatedEmailAddressesToNotifyOfStartup;
            return self();
        }
        
        @Override
        public BuilderT setMailFrom(String mailFrom) {
            this.mailFrom = mailFrom;
            return self();
        }

        @Override
        public BuilderT setMailSmtpHost(String mailSmtpHost) {
            this.mailSmtpHost = mailSmtpHost;
            return self();
        }

        @Override
        public BuilderT setMailSmtpPort(int mailSmtpPort) {
            this.mailSmtpPort = mailSmtpPort;
            return self();
        }

        @Override
        public BuilderT setMailSmtpAuth(boolean mailSmtpAuth) {
            this.mailSmtpAuth = mailSmtpAuth;
            return self();
        }

        @Override
        public BuilderT setMailSmtpUser(String mailSmtpUser) {
            this.mailSmtpUser = mailSmtpUser;
            return self();
        }

        @Override
        public BuilderT setMailSmtpPassword(String mailSmtpPassword) {
            this.mailSmtpPassword = mailSmtpPassword;
            return self();
        }

        protected Map<ProcessConfigurationVariable, String> getUserData() {
            final Map<ProcessConfigurationVariable, String> userData = new HashMap<>();
            getRelease().ifPresent(release->userData.putAll(release.getUserData()));
            userData.putAll(getDatabaseConfiguration().getUserData());
            userData.putAll(getOutboundReplicationConfiguration().getUserData());
            if (getServerName() != null) {
                userData.put(DefaultProcessConfigurationVariables.SERVER_NAME, getServerName());
            }
            getInboundReplicationConfiguration().ifPresent(inboundReplicationConfiguration->userData.putAll(inboundReplicationConfiguration.getUserData()));
            if (getCommaSeparatedEmailAddressesToNotifyOfStartup() != null) {
                userData.put(DefaultProcessConfigurationVariables.SERVER_STARTUP_NOTIFY, getCommaSeparatedEmailAddressesToNotifyOfStartup());
            }
            userData.put(DefaultProcessConfigurationVariables.MAIL_FROM, mailFrom==null?"noreply@sapsailing.com":mailFrom);
            userData.put(DefaultProcessConfigurationVariables.MAIL_SMTP_HOST, mailSmtpHost==null?getDefaultAwsSesMailHostForRegion():mailSmtpHost);
            userData.put(DefaultProcessConfigurationVariables.MAIL_SMTP_PORT, mailSmtpPort==null?DEFAULT_SMTP_PORT.toString():mailSmtpPort.toString());
            userData.put(DefaultProcessConfigurationVariables.MAIL_SMTP_AUTH, mailSmtpAuth==null?Boolean.TRUE.toString():mailSmtpAuth.toString());
            userData.put(DefaultProcessConfigurationVariables.MAIL_SMTP_USER, mailSmtpUser==null?DEFAULT_SMTP_USER:mailSmtpUser);
            if (mailSmtpPassword != null) {
                userData.put(DefaultProcessConfigurationVariables.MAIL_SMTP_PASSWORD, mailSmtpPassword);
            }
            return userData;
        }

        private String getDefaultAwsSesMailHostForRegion() {
            return String.format(DEFAULT_SMTP_HOSTNAME_PATTERN, getRegion().getId());
        }
    }

    private final Map<ProcessConfigurationVariable, String> userData;
    private final String serverName;
    
    protected AwsApplicationConfiguration(BuilderImpl<?, ?, ShardingKey, MetricsT, ProcessT> builder) {
        this.userData = Collections.unmodifiableMap(builder.getUserData());
        this.serverName = builder.getServerName();
    }

    @Override
    public Map<ProcessConfigurationVariable, String> getUserData() {
        return userData;
    }
    
    protected String getServerName() {
        return serverName;
    }
}
