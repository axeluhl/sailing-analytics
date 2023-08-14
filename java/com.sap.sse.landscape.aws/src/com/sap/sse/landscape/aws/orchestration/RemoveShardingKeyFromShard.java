package com.sap.sse.landscape.aws.orchestration;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsShard;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;

/**
 * This class is supposed to remove {@code shardingKeys} from the shard, indentified by {@shardName} from
 * {@code replicaSet}. This is done by rewriting all rules without {@code shardingKeys}'s path-conditions to the
 * replicaSet's load balancer. In the future, there should be a algorithm to extract and resort all rules without
 * removing them for ensuring that 100% of time the requests are reaching the shard's target group.
 * 
 * @author I569653
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 * @param <ProcessT>
 */
public class RemoveShardingKeyFromShard<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
        extends ShardProcedure<ShardingKey, MetricsT, ProcessT> {
    private static final Logger logger = Logger.getLogger(RemoveShardingKeyFromShard.class.getName());
    
    public RemoveShardingKeyFromShard(BuilderImpl<?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
    }

    static class BuilderImpl<BuilderT extends Builder<BuilderT, RemoveShardingKeyFromShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
            extends
            ShardProcedure.BuilderImpl<BuilderT, RemoveShardingKeyFromShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> {

        @Override
        public RemoveShardingKeyFromShard<ShardingKey, MetricsT, ProcessT> build() throws Exception {
            assert shardingKeys != null;
            assert replicaSet != null;
            assert region != null;
            return new RemoveShardingKeyFromShard<ShardingKey, MetricsT, ProcessT>(this);
        }
    }

    @Override
    public void run() throws Exception {
        AwsShard<ShardingKey> shard = null;
        for (Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> entry : replicaSet.getShards().entrySet()) {
            if (entry.getKey().getName().equals(shardName)) {
                shard = entry.getKey();
                break;
            }
        }
        if (shard == null) {
            throw new Exception("Shard not found!");
        }
        logger.info("Removing " + Util.joinStrings(", ", shardingKeys) + " from " + shardName);
        // remove conditions in rules where path is the sharding key
        final Set<ShardingKey> shardingKeysFromConditions = new HashSet<>();
        for (Rule r : shard.getRules()) {
            for (RuleCondition condition : r.conditions()) {
                if (condition.pathPatternConfig() != null) {
                    shardingKeysFromConditions.addAll(
                            Util.asList(
                                    Util.filter(
                                            Util.map(condition.values(), t-> ShardProcedure.getShardingKeyFromPathCondition(t)), 
                                            shardingKey -> !shardingKeys.contains(shardingKey))));
                } else {
                    logger.warning("This is strange: shard "+shard.getName()+" of replica set "+shard.getReplicaSetName()+
                            " has a rule "+r+" that has no path pattern condition; ignoring that rule while removing shard.");
                }
            }
        }
        if (shardingKeysFromConditions.isEmpty()) {
            // if the shard runs empty (no more sharding keys defined for it), set a proxy key to keep
            // the shard discoverable and its target group linked to the load balancer for continued target health checks
            shardingKeysFromConditions.add(SHARDING_KEY_UNUSED_BY_ANY_APPLICATION);
        }
        getLandscape().deleteLoadBalancerListenerRules(region, Util.toArray(shard.getRules(), new Rule[0]));
        // change ALB rules to new ones
        addShardingRules(shard.getLoadBalancer(), shardingKeysFromConditions, shard.getTargetGroup());
    }

    public static <MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, BuilderT extends Builder<BuilderT, RemoveShardingKeyFromShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey> Builder<BuilderT, RemoveShardingKeyFromShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT>();
    }
}