package com.sap.sse.landscape.aws.orchestration;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

/**
 * An abstract base class for procedures dealing with the public and master target groups in a
 * {@link ApplicationLoadBalancer load balancer}, e.g., creating them or fetching them, or
 * adding target to or removing targets from them.
 * 
 * @author Axel Uhl (D043530)
 */
public abstract class ProcedureWithTargetGroup<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
extends AbstractProcedureImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    private static final String MASTER_TARGET_GROUP_SUFFIX = "-m";
    private final ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed;
    private final String targetGroupNamePrefix;
    private final String serverName;
    private Iterable<Rule> rulesAdded;
    
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>,
    T extends ProcedureWithTargetGroup<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends com.sap.sse.common.Builder<BuilderT, T> {
        BuilderT setLoadBalancerUsed(ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed);
        BuilderT setTargetGroupNamePrefix(String targetGroupNamePrefix);
        BuilderT setServerName(String serverName);
        BuilderT setLandscape(AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape);
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>,
    T extends ProcedureWithTargetGroup<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    implements Builder<BuilderT, T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        private ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed;
        private String targetGroupNamePrefix;
        private String serverName;
        private AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape;
        
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

        public ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancerUsed() throws InterruptedException {
            return loadBalancerUsed;
        }

        public String getTargetGroupNamePrefix() {
            return targetGroupNamePrefix;
        }

        public String getServerName() throws JSchException, IOException, InterruptedException, SftpException {
            return serverName;
        }

        @Override
        public BuilderT setLandscape(
                AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
            this.landscape = landscape;
            return self();
        }

        public AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
            return landscape;
        }
    }

    protected ProcedureWithTargetGroup(BuilderImpl<?, ?, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder) throws JSchException, IOException, InterruptedException, SftpException {
        super(builder.getLandscape());
        this.loadBalancerUsed = builder.getLoadBalancerUsed();
        this.targetGroupNamePrefix = builder.getTargetGroupNamePrefix();
        this.serverName = builder.getServerName();
    }
    
    protected TargetGroup<ShardingKey, MetricsT> createTargetGroup(Region region, String targetGroupName,
            ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> process) {
        return getLandscape().createTargetGroup(getLoadBalancerUsed().getRegion(), targetGroupName,
                process.getPort(), process.getHealthCheckPath(),
                /* use traffic port as health check port, too */ process.getPort());
    }
    
    @Override
    public AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        return (AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>) super.getLandscape();
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
