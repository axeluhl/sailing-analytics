package com.sap.sse.landscape.aws.orchestration;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;

/**
 * An abstract base class for procedures dealing with the public and master target groups in a
 * {@link ApplicationLoadBalancer load balancer}, e.g., creating them or fetching them, or
 * adding target to or removing targets from them.
 * 
 * @author Axel Uhl (D043530)
 */
public abstract class ProcedureWithTargetGroup<ShardingKey>
extends AbstractAwsProcedureImpl<ShardingKey> {
    private static final String MASTER_TARGET_GROUP_SUFFIX = "-m";
    private final ApplicationLoadBalancer<ShardingKey> loadBalancerUsed;
    private final String targetGroupNamePrefix;
    private final String serverName;
    
    /**
     * If no {@link #setTargetGroupNamePrefix(String) target group name prefix} is specified, the target group names are
     * constructed from the {@link #setServerName(String) server name} property.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends ProcedureWithTargetGroup<ShardingKey>, ShardingKey>
    extends AbstractAwsProcedureImpl.Builder<BuilderT, T, ShardingKey> {
        BuilderT setLoadBalancerUsed(ApplicationLoadBalancer<ShardingKey> loadBalancerUsed);
        BuilderT setTargetGroupNamePrefix(String targetGroupNamePrefix);
        BuilderT setServerName(String serverName);
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends ProcedureWithTargetGroup<ShardingKey>, ShardingKey>
    extends AbstractAwsProcedureImpl.BuilderImpl<BuilderT, T, ShardingKey>
    implements Builder<BuilderT, T, ShardingKey> {
        private ApplicationLoadBalancer<ShardingKey> loadBalancerUsed;
        private String targetGroupNamePrefix;
        private String serverName;
        
        @Override
        public BuilderT setLoadBalancerUsed(
                ApplicationLoadBalancer<ShardingKey> loadBalancerUsed) {
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

        protected ApplicationLoadBalancer<ShardingKey> getLoadBalancerUsed() throws InterruptedException, ExecutionException {
            return loadBalancerUsed;
        }

        protected String getTargetGroupNamePrefix() {
            return targetGroupNamePrefix;
        }

        protected String getServerName() throws Exception {
            return serverName;
        }

        protected AwsLandscape<ShardingKey> getLandscape() {
            return (AwsLandscape<ShardingKey>) super.getLandscape();
        }
    }

    protected ProcedureWithTargetGroup(BuilderImpl<?, ?, ShardingKey> builder) throws Exception {
        super(builder);
        this.loadBalancerUsed = builder.getLoadBalancerUsed();
        this.targetGroupNamePrefix = builder.getTargetGroupNamePrefix();
        this.serverName = builder.getServerName();
    }
    
    protected <ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, MetricsT extends ApplicationProcessMetrics>
    TargetGroup<ShardingKey> createTargetGroup(Region region, String targetGroupName, ProcessT process) {
        return getLandscape().createTargetGroup(getLoadBalancerUsed().getRegion(), targetGroupName, process.getPort(),
                process.getHealthCheckPath(), /* use traffic port as health check port, too */ process.getPort(),
                getLoadBalancerUsed().getArn());
    }
    
    @Override
    public AwsLandscape<ShardingKey> getLandscape() {
        return (AwsLandscape<ShardingKey>) super.getLandscape();
    }

    public String getMasterTargetGroupName() {
        return getPublicTargetGroupName()+MASTER_TARGET_GROUP_SUFFIX;
    }
    
    public TargetGroup<ShardingKey> getMasterTargetGroup() throws JSchException, IOException, InterruptedException, SftpException {
        return getLandscape().getTargetGroup(loadBalancerUsed.getRegion(), getMasterTargetGroupName(),
                getLoadBalancerUsed().getArn());
    }
    
    public String getPublicTargetGroupName() {
        return targetGroupNamePrefix+serverName.replaceAll("_", "-");
    }
    
    public TargetGroup<ShardingKey> getPublicTargetGroup() throws JSchException, IOException, InterruptedException, SftpException {
        return getLandscape().getTargetGroup(loadBalancerUsed.getRegion(), getPublicTargetGroupName(),
                getLoadBalancerUsed().getArn());
    }
    
    public ApplicationLoadBalancer<ShardingKey> getLoadBalancerUsed() {
        return loadBalancerUsed;
    }
}
