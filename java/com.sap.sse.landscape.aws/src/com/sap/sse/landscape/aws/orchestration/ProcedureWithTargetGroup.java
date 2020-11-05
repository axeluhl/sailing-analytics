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
    private final String servername;
    private Iterable<Rule> rulesAdded;

    public ProcedureWithTargetGroup(ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed, String targetGroupNamePrefix,
            AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, String servername) throws JSchException, IOException, InterruptedException, SftpException {
        super(landscape);
        this.loadBalancerUsed = loadBalancerUsed;
        this.targetGroupNamePrefix = targetGroupNamePrefix;
        this.servername = servername;
    }
    
    protected TargetGroup<ShardingKey, MetricsT> createTargetGroup(Region region, String targetGroupName,
            ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> process) {
        return getLandscape().createTargetGroup(getLoadBalancerUsed().getRegion(), getPublicTargetGroupName(),
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
        return targetGroupNamePrefix+servername;
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
