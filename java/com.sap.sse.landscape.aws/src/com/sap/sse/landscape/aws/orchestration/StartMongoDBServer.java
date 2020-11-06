package com.sap.sse.landscape.aws.orchestration;

import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;

/**
 * Launches a MongoDB host with user data as follows:<pre>
 * REPLICA_SET_NAME={@link Builder#setReplicaSetName(String) replicaSetName}
 * REPLICA_SET_PRIMARY={@link Builder#setReplicaSetName(String) replicaSetPrimary}
 * REPLICA_SET_PRIORITY={@link Builder#setReplicaSetName(String) replicaSetVotes}
 * REPLICA_SET_VOTES={@link Builder#setReplicaSetName(String) replicaSetVotes}</pre>
 * 
 * @author Axel Uhl (D043530)
 */
public class StartMongoDBServer<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
extends StartAwsHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>> {
    private static enum MongoDBReplicaSetUserData {
        REPLICA_SET_NAME, REPLICA_SET_PRIMARY, REPLICA_SET_PRIORITY, REPLICA_SET_VOTES;
    }
    
    /**
     * Defaults:
     * <ul>
     * <li>The image type defaults to the value of the constant {@link #MONGODB_SERVER_IMAGE_TYPE} ("mongodb-server").</li>
     * <li>The replica set name defaults to "live"</li>
     * <li>The replica set priority defaults to 1</li>
     * <li>The replica set votes defaults to 1</li>
     * </ul>
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    extends StartAwsHost.Builder<StartMongoDBServer<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>> {
        /**
         * The default {@link #setImageType(String) image type} used for launching a MongoDB server.
         */
        String MONGODB_SERVER_IMAGE_TYPE = "mongodb-server";
        
        Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> setReplicaSetName(String replicaSetName);
        
        Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> setReplicaSetPrimary(String replicaSetPrimary);
        
        Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> setReplicaSetPriority(int replicaSetPriority);
        
        Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> setReplicaSetVotes(int replicaSetVotes);
        
        StartMongoDBServer<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> build();
    }
    
    protected static class BuilderImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    extends StartAwsHost.BuilderImpl<StartMongoDBServer<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>>
    implements Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
        private String replicaSetName = "live";
        private String replicaSetPrimary;
        private int replicaSetPriority = 1;
        private int replicaSetVotes = 1;

        private BuilderImpl() {
            setImageType(MONGODB_SERVER_IMAGE_TYPE);
        }
        
        @Override
        public HostSupplier<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>> getHostSupplier() {
            return AwsInstanceImpl::new;
        }

        @Override
        public Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> setReplicaSetName(String replicaSetName) {
            this.replicaSetName = replicaSetName;
            return this;
        }

        @Override
        public Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> setReplicaSetPrimary(String replicaSetPrimary) {
            this.replicaSetPrimary = replicaSetPrimary;
            return this;
        }

        @Override
        public Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> setReplicaSetPriority(int replicaSetPriority) {
            this.replicaSetPriority = replicaSetPriority;
            return this;
        }

        @Override
        public Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> setReplicaSetVotes(int replicaSetVotes) {
            this.replicaSetVotes = replicaSetVotes;
            return this;
        }

        protected String getReplicaSetName() {
            return replicaSetName;
        }

        protected String getReplicaSetPrimary() {
            return replicaSetPrimary;
        }

        protected int getReplicaSetPriority() {
            return replicaSetPriority;
        }

        protected int getReplicaSetVotes() {
            return replicaSetVotes;
        }
        
        @Override
        public StartMongoDBServer<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> build() {
            return new StartMongoDBServer<>(this);
        }
    }
    
    public static <ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> builder() {
        return new BuilderImpl<>();
    }
    
    protected StartMongoDBServer(BuilderImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> builder) {
        super(builder);
        addUserData(MongoDBReplicaSetUserData.REPLICA_SET_NAME.name(), builder.getReplicaSetName());
        addUserData(MongoDBReplicaSetUserData.REPLICA_SET_PRIMARY.name(), builder.getReplicaSetPrimary());
        addUserData(MongoDBReplicaSetUserData.REPLICA_SET_PRIORITY.name(), Integer.toString(builder.getReplicaSetPriority()));
        addUserData(MongoDBReplicaSetUserData.REPLICA_SET_VOTES.name(), Integer.toString(builder.getReplicaSetVotes()));
    }

}
