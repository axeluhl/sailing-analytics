package com.sap.sse.landscape.aws.orchestration;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerStateEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;

/**
 * For an {@link ApplicationProcess} creates a set of rules in an {@link ApplicationLoadBalancer} which drives traffic
 * to one of the two {@link TargetGroup}s that this procedure will also create, registering the process as the first
 * target in both groups. One target group will take the traffic for the single "master" node; the other target group
 * will take the traffic for all public-facing nodes which by default in the minimal application server replica set
 * configuration will be the single master node. As the number of replicas grows, the master may choose to only serve
 * the writing requests and be removed from the public-facing target group again.
 * <p>
 * 
 * The rules that this procedure creates are currently the following:
 * <ol>
 * <li>If the HTTP header field identified by {@link HttpRequestHeaderConstants#HEADER_KEY_FORWARD_TO} has value
 * {@link HttpRequestHeaderConstants#HEADER_FORWARD_TO_MASTER} and the hostname header matches then forward to the
 * "master" target group</li>
 * <li>If the HTTP header field identified by {@link HttpRequestHeaderConstants#HEADER_KEY_FORWARD_TO} has value
 * {@link HttpRequestHeaderConstants#HEADER_FORWARD_TO_REPLICA} and the hostname header matches then forward to the
 * "public" target group</li>
 * <li>If the HTTP request method is {@code GET} and the hostname header matches then forward to the "public" target
 * group</li>
 * <li>Forward all other requests with a matching hostname header to the "master" target group</li>
 * </ol>
 * <p>
 * 
 * The target groups are set up to use HTTP as the protocol in case the {@link ApplicationProcess#getPort() port
 * specified} is anything but 443, such that SSL offloading will happen at the load balancer in this case. The target
 * group names are limited to 32 characters in length. Target group name prefixes should be chosen to be very short
 * strings in order not to unnecessarily limit the number of characters available for application server replica set
 * naming.
 * <p>
 * 
 * The {@link ApplicationProcess#getPort() port} and the {@link ApplicationProcess#getHealthCheckPath() health check
 * path} are taken from the {@link ApplicationProcess}. The application process's {@link ApplicationProcess#getHost()
 * host} is then added to both target groups.
 * <p>
 * 
 * The default health check settings for the target groups created are:
 * <ul>
 * <li>healthy threshold: 2</li>
 * <li>unhealthy threshold: 2</li>
 * <li>timeout: 4s</li>
 * <li>interval: 5s</li>
 * <li>success codes: 200</li>
 * </ul>
 * 
 * @author Axel Uhl (D043530)
 */
public abstract class CreateLoadBalancerMapping<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
extends ProcedureWithTargetGroup<ShardingKey, MetricsT, ProcessT, HostT> {
    protected static int NUMBER_OF_RULES_PER_REPLICA_SET = 4;
    protected static final int MAX_RULES_PER_ALB = 100;
    protected static final int MAX_ALBS_PER_REGION = 20;
    private final ProcessT process;
    private final String hostname;
    private TargetGroup<ShardingKey, MetricsT> masterTargetGroupCreated;
    private TargetGroup<ShardingKey, MetricsT> publicTargetGroupCreated;
    private Iterable<Rule> rulesAdded;
    
    /**
     * Default rules implemented by this builder:
     * <ul>
     * <li>The {@link #setServerName(String) server name} property will be obtained from the {@link #setProcess(ApplicationProcess) process}'s
     * {@code SERVER_NAME} environment setting if not provided explicitly.</li>
     * <li>The timeout for looking up the process's server name defaults to no timeout.</li>
     * <li>If no {@link #setKeyName(String) SSH key pair name} is specified, the key pair used to launch the
     * instance that runs the {@link #setProcess(ApplicationProcess) application process} will be looked up and
     * decrypted using the {@link #setPrivateKeyEncryptionPassphrase(byte[]) passphrase} that must be provided.
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends CreateLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends ProcedureWithTargetGroup.Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT> {
        BuilderT setProcess(ProcessT process);
        BuilderT setHostname(String hostname);
        BuilderT setTimeout(Duration timeout);
        BuilderT setKeyName(String keyName);
        BuilderT setPrivateKeyEncryptionPassphrase(byte[] privateKeyEncryptionPassphrase);
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends CreateLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends ProcedureWithTargetGroup.BuilderImpl<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>
    implements Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT> {
        private static final Logger logger = Logger.getLogger(BuilderImpl.class.getName());
        private String hostname;
        private ProcessT process;
        private Optional<Duration> optionalTimeout = Optional.empty();
        private Optional<String> optionalKeyName = Optional.empty(); // if empty, SSH key pair used to start the instance hosting the process will be used
        private byte[] privateKeyEncryptionPassphrase;

        @Override
        public BuilderT setProcess(ProcessT process) {
            this.process = process;
            return self();
        }
        
        @Override
        public BuilderT setKeyName(String keyName) {
            this.optionalKeyName = Optional.ofNullable(keyName);
            return self();
        }
        
        protected Optional<String> getOptionalKeyName() {
            return optionalKeyName;
        }

        @Override
        public BuilderT setHostname(String hostname) {
            this.hostname = hostname;
            return self();
        }

        protected String getHostname() {
            return hostname;
        }

        protected ProcessT getProcess() {
            return process;
        }

        @Override
        public BuilderT setTimeout(Duration timeout) {
            this.optionalTimeout = Optional.of(timeout);
            return self();
        }

        protected Optional<Duration> getOptionalTimeout() {
            return optionalTimeout;
        }
        
        @Override
        public BuilderT setPrivateKeyEncryptionPassphrase(byte[] privateKeyEncryptionPassphrase) {
            this.privateKeyEncryptionPassphrase = privateKeyEncryptionPassphrase;
            return self();
        }

        @Override
        protected String getServerName() throws Exception {
            final String result;
            if (super.getServerName() != null) {
                result = super.getServerName();
            } else {
                result = getProcess().getServerName(getOptionalTimeout(), getOptionalKeyName(), privateKeyEncryptionPassphrase);
            }
            return result;
        }

        protected void waitUntilLoadBalancerProvisioned(AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape, ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancer) throws InterruptedException {
            final TimePoint startingToPollForReady = TimePoint.now();
            while (landscape.getApplicationLoadBalancerStatus(loadBalancer).code() == LoadBalancerStateEnum.PROVISIONING
                    && (!getOptionalTimeout().isPresent() || startingToPollForReady.until(TimePoint.now()).compareTo(getOptionalTimeout().get()) <= 0)) {
                logger.info("Application load balancer "+loadBalancer.getName()+" still PROVISIONING. Waiting...");
                Thread.sleep(5000); // wait until the ALB has been provisioned or failed
            }
        }
    }

    protected CreateLoadBalancerMapping(BuilderImpl<?, ?, ShardingKey, MetricsT, ProcessT, HostT> builder) throws Exception {
        super(builder);
        this.process = builder.getProcess();
        this.hostname = builder.getHostname();
    }
    
    @Override
    public AwsLandscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
        return (AwsLandscape<ShardingKey, MetricsT, ProcessT>) super.getLandscape();
    }

    @Override
    public void run() throws JSchException, IOException, InterruptedException, SftpException {
        masterTargetGroupCreated = createTargetGroup(getLoadBalancerUsed().getRegion(), getMasterTargetGroupName(), getProcess());
        publicTargetGroupCreated = createTargetGroup(getLoadBalancerUsed().getRegion(), getPublicTargetGroupName(), getProcess());
        getLandscape().addTargetsToTargetGroup(masterTargetGroupCreated, Collections.singleton(getHost()));
        getLandscape().addTargetsToTargetGroup(publicTargetGroupCreated, Collections.singleton(getHost()));
        getLoadBalancerUsed().addRulesAssigningUnusedPriorities(/* forceContiguous */ true, createRules());
    }
    
    private Rule[] createRules() {
        final Rule[] rules = new Rule[NUMBER_OF_RULES_PER_REPLICA_SET];
        int ruleCount = 0;
        rules[ruleCount++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-header").httpHeaderConfig(hhcb->hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO).values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER.getA())).build(),
                RuleCondition.builder().field("host-header").hostHeaderConfig(hhcb->hhcb.values(getHostName())).build()).
                actions(Action.builder().type(ActionTypeEnum.FORWARD).forwardConfig(fc->fc.targetGroups(TargetGroupTuple.builder().targetGroupArn((getMasterTargetGroupCreated().getTargetGroupArn())).build())).build()).
                build();
        rules[ruleCount++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-header").httpHeaderConfig(hhcb->hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO).values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getA())).build(),
                RuleCondition.builder().field("host-header").hostHeaderConfig(hhcb->hhcb.values(getHostName())).build()).
                actions(Action.builder().type(ActionTypeEnum.FORWARD).forwardConfig(fc->fc.targetGroups(TargetGroupTuple.builder().targetGroupArn((getPublicTargetGroupCreated().getTargetGroupArn())).build())).build()).
                build();
        rules[ruleCount++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-request-method").httpRequestMethodConfig(hrmcb->hrmcb.values("GET")).build(),
                RuleCondition.builder().field("host-header").hostHeaderConfig(hhcb->hhcb.values(getHostName())).build()).
                actions(Action.builder().type(ActionTypeEnum.FORWARD).forwardConfig(fc->fc.targetGroups(TargetGroupTuple.builder().targetGroupArn((getPublicTargetGroupCreated().getTargetGroupArn())).build())).build()).
                build();
        rules[ruleCount++] = Rule.builder().conditions(
                RuleCondition.builder().field("host-header").hostHeaderConfig(hhcb->hhcb.values(getHostName())).build()).
                actions(Action.builder().type(ActionTypeEnum.FORWARD).forwardConfig(fc->fc.targetGroups(TargetGroupTuple.builder().targetGroupArn((getMasterTargetGroupCreated().getTargetGroupArn())).build())).build()).
                build();
        assert ruleCount == NUMBER_OF_RULES_PER_REPLICA_SET;
        return rules;
    }

    protected String getHostName() {
        return hostname;
    }

    private AwsInstance<ShardingKey, MetricsT> getHost() {
        @SuppressWarnings("unchecked")
        final AwsInstance<ShardingKey, MetricsT> result = (AwsInstance<ShardingKey, MetricsT>) getProcess().getHost();
        return result;
    }
    
    public TargetGroup<ShardingKey, MetricsT> getMasterTargetGroupCreated() {
        return masterTargetGroupCreated;
    }

    public TargetGroup<ShardingKey, MetricsT> getPublicTargetGroupCreated() {
        return publicTargetGroupCreated;
    }

    public Iterable<Rule> getRulesAdded() {
        return rulesAdded;
    }

    public ProcessT getProcess() {
        return process;
    }

    protected static String getHostedZoneName(String hostname) {
        return hostname.substring(hostname.indexOf('.')+1);
    }
}
