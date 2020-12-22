package com.sap.sse.landscape.aws.orchestration;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

/**
 * An abstract base class for procedures dealing with the public and master target groups in a
 * {@link ApplicationLoadBalancer load balancer}, e.g., creating them or fetching them, or
 * adding target to or removing targets from them.
 * 
 * @author Axel Uhl (D043530)
 */
public abstract class ProcedureWithTargetGroup<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
extends AbstractAwsProcedureImpl<ShardingKey, MetricsT, ProcessT> {
    private static final String MASTER_TARGET_GROUP_SUFFIX = "-m";
    private final ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed;
    private final String targetGroupNamePrefix;
    private final String serverName;
    private Iterable<Rule> rulesAdded;
    
    /**
     * If no {@link #setTargetGroupNamePrefix(String) target group name prefix} is specified, the target group names are
     * constructed from the {@link #setServerName(String) server name} property.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends ProcedureWithTargetGroup<ShardingKey, MetricsT, ProcessT, HostT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends AbstractAwsProcedureImpl.Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
        BuilderT setLoadBalancerUsed(ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed);
        BuilderT setTargetGroupNamePrefix(String targetGroupNamePrefix);
        BuilderT setServerName(String serverName);
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends ProcedureWithTargetGroup<ShardingKey, MetricsT, ProcessT, HostT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends AbstractAwsProcedureImpl.BuilderImpl<BuilderT, T, ShardingKey, MetricsT, ProcessT>
    implements Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT> {
        private ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed;
        private String targetGroupNamePrefix;
        private String serverName;
        
        @Override
        public BuilderT setLoadBalancerUsed(
                ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed) {
            this.loadBalancerUsed = loadBalancerUsed;
            return self();
        }

        @Override
        public BuilderT setTargetGroupNamePrefix(
                String targetGroupNamePrefix) {
            this.targetGroupNamePrefix = targetGroupNamePrefix;
            return self();
        }

        @Override
        public BuilderT setServerName(String serverName) {
            this.serverName = serverName;
            return self();
        }

        protected ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancerUsed() throws InterruptedException {
            return loadBalancerUsed;
        }

        protected String getTargetGroupNamePrefix() {
            return targetGroupNamePrefix;
        }

        protected String getServerName() throws JSchException, IOException, InterruptedException, SftpException {
            return serverName;
        }

        protected AwsLandscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
            return (AwsLandscape<ShardingKey, MetricsT, ProcessT>) super.getLandscape();
        }
    }

    protected ProcedureWithTargetGroup(BuilderImpl<?, ?, ShardingKey, MetricsT, ProcessT, HostT> builder) throws JSchException, IOException, InterruptedException, SftpException {
        super(builder);
        this.loadBalancerUsed = builder.getLoadBalancerUsed();
        this.targetGroupNamePrefix = builder.getTargetGroupNamePrefix();
        this.serverName = builder.getServerName();
    }
    
    protected TargetGroup<ShardingKey, MetricsT> createTargetGroup(Region region, String targetGroupName, ProcessT process) {
        return getLandscape().createTargetGroup(getLoadBalancerUsed().getRegion(), targetGroupName,
                process.getPort(), process.getHealthCheckPath(),
                /* use traffic port as health check port, too */ process.getPort()); // TODO this doesn't health-check the reverse proxy running on the instance for default set-ups with only one process running on the instance; but how do we know?
    }
    
    @Override
    public AwsLandscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
        return (AwsLandscape<ShardingKey, MetricsT, ProcessT>) super.getLandscape();
    }

    protected String getMasterTargetGroupName() {
        return getPublicTargetGroupName()+MASTER_TARGET_GROUP_SUFFIX;
    }
    
    protected TargetGroup<ShardingKey, MetricsT> getMasterTargetGroup() throws JSchException, IOException, InterruptedException, SftpException {
        return getLandscape().getTargetGroup(loadBalancerUsed.getRegion(), getMasterTargetGroupName());
    }
    
    protected String getPublicTargetGroupName() {
        return targetGroupNamePrefix+serverName;
    }
    
    protected TargetGroup<ShardingKey, MetricsT> getPublicTargetGroup() throws JSchException, IOException, InterruptedException, SftpException {
        return getLandscape().getTargetGroup(loadBalancerUsed.getRegion(), getPublicTargetGroupName());
    }
    
    public ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancerUsed() {
        return loadBalancerUsed;
    }

    public Iterable<Rule> getRulesAdded() {
        return rulesAdded;
    }

    protected static String getHostedZoneName(String hostname) {
        return hostname.substring(hostname.indexOf('.')+1);
    }
}
