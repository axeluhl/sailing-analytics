package com.sap.sse.landscape.aws.orchestration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;

import com.jcraft.jsch.JSchException;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;
import com.sap.sse.landscape.mongodb.impl.MongoProcessImpl;
import com.sap.sse.landscape.mongodb.impl.MongoProcessInReplicaSetImpl;
import com.sap.sse.landscape.orchestration.StartHost;

import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Launches a MongoDB host with a single MongoDB process listening on the {@link MongoProcess#DEFAULT_PORT default port}
 * with user data as follows:
 * 
 * <pre>
 * REPLICA_SET_NAME={@link Builder#setReplicaSetName(String) replicaSetName}
 * REPLICA_SET_PRIMARY={@link Builder#setReplicaSetName(String) replicaSetPrimary}
 * REPLICA_SET_PRIORITY={@link Builder#setReplicaSetName(String) replicaSetVotes}
 * REPLICA_SET_VOTES={@link Builder#setReplicaSetName(String) replicaSetVotes}
 * </pre>
 * 
 * If this is used to add a replica to an already existing replica set, specifying the replica set name is sufficient,
 * and the existing nodes will be discovered by searching for hosts tagged with the tag whose name is provided by the
 * {@link #MONGODB_REPLICA_SETS_TAG_NAME} constant.
 * <p>
 * 
 * To start a primary, only specify the desired replica set name and leave the primary specification empty {@code null}.
 * In this case no other node tagged for the desired replica set will be found, and the new instance will become the
 * first primary of the new replica set.<p>
 * 
 * TODO Why is the MetricsT parameter necessary when there is MongoMetrics?
 * 
 * @author Axel Uhl (D043530)
 */
public class StartMongoDBServer<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends StartAwsHost<ShardingKey, MetricsT, ProcessT, AwsInstance<ShardingKey, MetricsT>> {
    private static enum MongoDBReplicaSetUserData implements ProcessConfigurationVariable {
        REPLICA_SET_NAME, REPLICA_SET_PRIMARY, REPLICA_SET_PRIORITY, REPLICA_SET_VOTES;
    }
    
    private MongoProcess mongoProcess;
    
    private final String replicaSetName;

    private final  Optional<Duration> optionalTimeout;
    
    /**
     * In order to launch a standalone instance, call {@link #setReplicaSetName(String)} with {@code null} as a
     * parameter. To launch a new first instance (primary) for a replica set call {@link #setReplicaSetPrimary(String)}
     * with {@code null} as a parameter. If you use an {@link Builder#setImageType(String) image type} that supports NVMe
     * storage (on-board fast SSDs) then your MongoDB may have plenty and fast but only ephemeral storage. This is suitable
     * only for replicas where your data is protected by spreading it across availability zones and/or having at least one
     * replica in the replica set that has a non-ephemeral volume as the basis for storage which can also undergo EBS
     * snapshot backups.<p>
     * 
     * Defaults:
     * <ul>
     * <li>The image type, indicated by the tag whose name is provided by the constant
     * {@link StartHost#IMAGE_TYPE_TAG_NAME}, defaults to the value of the constant {@link #MONGODB_SERVER_IMAGE_TYPE}
     * ("mongodb-server").</li>
     * <li>The replica set name defaults to {@link AwsLandscape#MONGO_DEFAULT_REPLICA_SET_NAME "live"}</li>
     * <li>The "primary" information, if not provided (and explicitly {@link #setReplicaSetPrimary(String) setting} to
     * {@code null} counts as "providing" in this case), is obtained by searching the region for instances that based on their
     * {@link AwsLandscape#MONGO_DEFAULT_REPLICA_SET_NAME} tag declare to belong to the replica set selected, connecting to the
     * replica set and figuring out its primary node.</li>
     * <li>The replica set priority defaults to 1</li>
     * <li>The replica set votes defaults to 1</li>
     * <li>A tag is added to the instance launched that lets the landscape recognize it as running a MongoDB process</li>
     * <li>The instance name (the "Name" tag's value) defaults to "MongoDB Replica Set {replica-set-name} P{replica-set-priority}"</li>
     * <li>The instance type defaults to {@link InstanceType#I3_LARGE} which is a small instance type with NVMe storage that is storage
     * optimized and hence provides good throughput to the NVMe disk</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey, MetricsT, ProcessT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends StartAwsHost.Builder<BuilderT, StartMongoDBServer<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT, AwsInstance<ShardingKey, MetricsT>> {
        /**
         * The default {@link #setImageType(String) image type} used for launching a MongoDB server.
         */
        String MONGODB_SERVER_IMAGE_TYPE = "mongodb-server";
        
        BuilderT setReplicaSetName(String replicaSetName);
        
        BuilderT setReplicaSetPrimary(String replicaSetPrimary);
        
        BuilderT setReplicaSetPriority(int replicaSetPriority);
        
        BuilderT setReplicaSetVotes(int replicaSetVotes);
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey, MetricsT, ProcessT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends StartAwsHost.BuilderImpl<BuilderT, StartMongoDBServer<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT, AwsInstance<ShardingKey, MetricsT>>
    implements Builder<BuilderT, ShardingKey, MetricsT, ProcessT> {
        private String replicaSetName = AwsLandscape.MONGO_DEFAULT_REPLICA_SET_NAME;
        private String replicaSetPrimary;
        private boolean replicaSetPrimaryWasSetExplicitly = false;
        private int replicaSetPriority = 1;
        private int replicaSetVotes = 1;

        private BuilderImpl() {
            setImageType(MONGODB_SERVER_IMAGE_TYPE);
        }
        
        @Override
        public HostSupplier<ShardingKey, MetricsT, ProcessT, AwsInstance<ShardingKey, MetricsT>> getHostSupplier() {
            return AwsInstanceImpl::new;
        }

        @Override
        public BuilderT setReplicaSetName(String replicaSetName) {
            this.replicaSetName = replicaSetName;
            return self();
        }

        @Override
        public BuilderT setReplicaSetPrimary(String replicaSetPrimary) {
            this.replicaSetPrimary = replicaSetPrimary;
            this.replicaSetPrimaryWasSetExplicitly = true;
            return self();
        }

        @Override
        public BuilderT setReplicaSetPriority(int replicaSetPriority) {
            this.replicaSetPriority = replicaSetPriority;
            return self();
        }

        @Override
        public BuilderT setReplicaSetVotes(int replicaSetVotes) {
            this.replicaSetVotes = replicaSetVotes;
            return self();
        }

        protected String getReplicaSetName() {
            return replicaSetName;
        }

        protected String getReplicaSetPrimary() throws URISyntaxException, JSchException, IOException, InterruptedException {
            final String result;
            if (replicaSetPrimary == null) {
                if (replicaSetPrimaryWasSetExplicitly) {
                    // a new first primary for a (presumably) new replica set or a new stand-alone instance
                    result = null;
                } else {
                    if (getReplicaSetName() != null) {
                        // no primary information was set explicitly; take all instances known so far and construct a comma-separated host:port list
                        // for connecting to the entire replica set through which then the primary is automatically found for adding the replica to the set
                        result = String.join(",", Util.map(Util.filter(getLandscape().getDatabaseConfigurationForReplicaSet(getRegion(), getReplicaSetName()).getInstances(),
                                instance->instance.getHost().getPrivateAddress(getOptionalTimeout()) != null), 
                                instance->instance.getHost().getPrivateAddress(getOptionalTimeout()).getCanonicalHostName()+":"+instance.getPort()));
                    } else {
                        result = null; // empty replica set name provided, overriding default replica set name; launch as stand-alone 
                    }
                }
            } else {
                if (getReplicaSetName() == null) {
                    throw new IllegalStateException("Cannot provide a primary address " + replicaSetPrimary
                            + " when no replica set name is specified which means to create a stand-alone process");
                } else {
                    result = replicaSetPrimary;
                }
            }
            return result;
        }

        protected int getReplicaSetPriority() {
            return replicaSetPriority;
        }

        protected int getReplicaSetVotes() {
            return replicaSetVotes;
        }
        
        @Override
        protected String getInstanceName() {
            final String result;
            if (super.getInstanceName() != null) {
                result = super.getInstanceName();
            } else {
                result = "MongoDB Replica Set "+getReplicaSetName()+" P"+getReplicaSetPriority();
            }
            return result;
        }
        
        @Override
        protected InstanceType getInstanceType() {
            final InstanceType result;
            if (super.getInstanceType() == null) {
                result = InstanceType.I3_LARGE;
            } else {
                result = super.getInstanceType();
            }
            return result;
        }
        
        @Override
        public StartMongoDBServer<ShardingKey, MetricsT, ProcessT> build() throws URISyntaxException, JSchException, IOException, InterruptedException {
            setTags(getLandscape().getTagForMongoProcess(getTags().orElse(Tags.empty()), getReplicaSetName(), MongoProcess.DEFAULT_PORT));
            if (!isSecurityGroupsSet()) {
                setSecurityGroups(Collections.singleton(getLandscape().getDefaultSecurityGroupForMongoDBHosts(getRegion())));
            }
            return new StartMongoDBServer<>(this);
        }
        
        /**
         * Re-expose the method declared protected in a different package to the local procedure
         */
        @Override
        protected Optional<Duration> getOptionalTimeout() {
            return super.getOptionalTimeout();
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey, MetricsT, ProcessT>,ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    Builder<BuilderT, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<>();
    }
    
    protected StartMongoDBServer(BuilderImpl<?, ShardingKey, MetricsT, ProcessT> builder) throws URISyntaxException, JSchException, IOException, InterruptedException {
        super(builder);
        this.replicaSetName = builder.getReplicaSetName();
        if (builder.getReplicaSetName() != null) {
            addUserData(MongoDBReplicaSetUserData.REPLICA_SET_NAME, builder.getReplicaSetName());
        }
        if (builder.getReplicaSetPrimary() != null) {
            addUserData(MongoDBReplicaSetUserData.REPLICA_SET_PRIMARY, builder.getReplicaSetPrimary());
        }
        addUserData(MongoDBReplicaSetUserData.REPLICA_SET_PRIORITY, Integer.toString(builder.getReplicaSetPriority()));
        addUserData(MongoDBReplicaSetUserData.REPLICA_SET_VOTES, Integer.toString(builder.getReplicaSetVotes()));
        this.optionalTimeout = builder.getOptionalTimeout();
    }

    /**
     * The process launched by this procedure. {@link MongoProcess#getHost()} is expected to match {@link #getHost()}.
     */
    public MongoProcess getMongoProcess() {
        return mongoProcess;
    }

    @Override
    public void run() throws Exception {
        super.run();
        if (replicaSetName == null) {
            mongoProcess = new MongoProcessImpl(getHost());
        } else {
            // the host may not yet be in state RUNNING; we'd like to wait for this:
            boolean running = Util.wait(()->getLandscape().getInstance(getHost().getInstanceId(), getHost().getRegion()).state().name() == InstanceStateName.RUNNING,
                    optionalTimeout, Duration.ONE_SECOND.times(5), Level.INFO, "Waiting for host "+getHost().getInstanceId()+" to be in state RUNNING");
            if (!running) {
                throw new IllegalStateException("The host launched did not reach state RUNNING"+optionalTimeout.map(d->" within timeout "+d).orElse(""));
            }
            final MongoReplicaSet replicaSet = getLandscape().getDatabaseConfigurationForReplicaSet(getHost().getRegion(), replicaSetName);
            final Host instance = Util.stream(replicaSet.getInstances()).filter(replica->replica.getHost().equals(getHost())).map(replica->replica.getHost()).findAny().get();
            mongoProcess = new MongoProcessInReplicaSetImpl(replicaSet, MongoProcess.DEFAULT_PORT, instance);
        }
    }
}
