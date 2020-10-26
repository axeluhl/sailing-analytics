package com.sap.sse.landscape.aws.orchestration;

import java.util.Collections;

import com.sap.sse.common.HttpRequestHeaderConstants;
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
 * For an {@link ApplicationProcess} creates a set of rules in an {@link ApplicationLoadBalancer} which drives traffic
 * to one of the two {@link TargetGroup}s that this procedure will also create. One will take the traffic for the single
 * "master" node; the other will take the traffic for all public-facing nodes which by default in the minimal
 * application server replica set configuration will be the single master node. As the number of replicas grows, the
 * master may choose to only serve the writing requests and be removed from the public-facing target group again.
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
 * The target groups are set up to use HTTP as the protocol such that SSL offloading will happen at the load balancer.
 * The target group names are limited to 32 characters in length. Target group name prefixes should be chosen to be
 * very short strings in order not to unnecessarily limit the number of characters available for application server
 * replica set naming.
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
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
        extends AbstractProcedureImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    private static final String MASTER_TARGET_GROUP_SUFFIX = "-m";
    private final ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> process;
    private final ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed;
    private final String targetGroupNamePrefix;
    private TargetGroup<ShardingKey, MetricsT> masterTargetGroupCreated;
    private TargetGroup<ShardingKey, MetricsT> publicTargetGroupCreated;
    private Iterable<Rule> rulesAdded;

    public CreateLoadBalancerMapping(ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> process,
            ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancerUsed,
            String targetGroupNamePrefix, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        super(landscape);
        this.loadBalancerUsed = loadBalancerUsed;
        this.targetGroupNamePrefix = targetGroupNamePrefix;
        this.process = process;
    }
    
    @Override
    public AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        return (AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>) super.getLandscape();
    }

    @Override
    public void run() {
        masterTargetGroupCreated = getLandscape().createTargetGroup(loadBalancerUsed.getRegion(),
                getMasterTargetGroupName(), getProcess().getPort(), getProcess().getHealthCheckPath(),
                /* use traffic port as health check port, too */ getProcess().getPort());
        publicTargetGroupCreated = getLandscape().createTargetGroup(loadBalancerUsed.getRegion(),
                getPublicTargetGroupName(), getProcess().getPort(), getProcess().getHealthCheckPath(),
                /* use traffic port as health check port, too */ getProcess().getPort());
        getLandscape().addTargetsToTargetGroup(masterTargetGroupCreated, Collections.singleton(getHost()));
        getLandscape().addTargetsToTargetGroup(publicTargetGroupCreated, Collections.singleton(getHost()));
        getLoadBalancerUsed().addRules(createRules());
        // TODO Implement CreateLoadBalancerMapping.run(...)
    }
    
    private Rule[] createRules() {
        final Rule[] rules = new Rule[4];
        // TODO continue here with implementing the four default load balancer rules...
        return rules;
    }

    private AwsInstance<ShardingKey, MetricsT> getHost() {
        @SuppressWarnings("unchecked")
        final AwsInstance<ShardingKey, MetricsT> result = (AwsInstance<ShardingKey, MetricsT>) getProcess().getHost();
        return result;
    }
    
    private String getMasterTargetGroupName() {
        return getPublicTargetGroupName()+MASTER_TARGET_GROUP_SUFFIX;
    }
    
    private String getPublicTargetGroupName() {
        return targetGroupNamePrefix+getProcess().getServerName();
    }
    
    public ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancerUsed() {
        return loadBalancerUsed;
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

    public ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getProcess() {
        return process;
    }
}
