package com.sap.sse.landscape.orchestration;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;

/**
 * Encodes a potentially compound sequence of actions that transform the landscape from one state to another, trying to
 * achieve a certain goal. For example, there may be a procedure for setting up a dedicated master application server
 * which is a component of another procedure which sets up an application server replica set which in turn is part of a
 * procedure for not only setting the replica set up but also wiring it with an "Ingress" in a combination of DNS
 * records, load balancer rules, target groups, necessary assignments of the instances to target groups, as well as
 * reverse proxy configurations on each application server replica set node.
 * <p>
 * 
 * Other procedures may set up a MongoDB replica set which refers to a procedure for setting up a single MongoDB
 * instance, or for archiving an event, or for scaling out an application server replica set by adding more replicas to
 * it.
 * <p>
 * 
 * A procedure may succeed or it may fail. Ideally, procedures support reverting their actions. This way, if a composite
 * procedure fails half way through, sub-procedures may be reverted to leave the landscape again in a consistent state.
 * <p>
 * 
 * Procedures should log their actions verbosely and in the log messages clearly tell which steps were taken and whether
 * or not they succeeded.
 * <p>
 * 
 * A procedure may be parameterizable. For example, a procedure for setting up a dedicated application server replica
 * set for an event could offer as its parameters an event-specific hostname / sub-domain name to be used, as well as a
 * name for a corresponding MongoDB database, or even offer to infer all that from a single event name such as "kw2020".
 * Further typical and common parameters for a procedure could be landscape-oriented specifications such as the region
 * or availability zone to which to deploy something.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Procedure<ShardingKey, 
                           MetricsT extends ApplicationProcessMetrics,
                           MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                           ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
extends Runnable {
    Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape();
}
