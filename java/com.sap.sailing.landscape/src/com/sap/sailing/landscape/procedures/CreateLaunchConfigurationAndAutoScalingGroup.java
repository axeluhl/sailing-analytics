package com.sap.sailing.landscape.procedures;

import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.orchestration.AwsApplicationConfiguration;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * For an {@link ApplicationReplicaSet} and a {@link TargetGroup} that represents the application replica set's
 * public target group creates an AWS EC2 Launch Configuration and a corresponding Auto-Scaling Group which by
 * default produces a minimum of one replica, a maximum of 30 replicas, scaling based on the number of requests
 * per target which are supposed to not exceed 30,000.<p>
 * 
 * The builder for the procedure requires the bearer token for replication permissions and the target group. Other properties
 * are optional.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 */
public class CreateLaunchConfigurationAndAutoScalingGroup<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey,MetricsT,ProcessT>>
extends AbstractProcedureImpl<ShardingKey>
implements Procedure<ShardingKey> {
    public static interface Builder<ShardingKey, BuilderT extends Builder<ShardingKey, BuilderT, MetricsT, ProcessT>, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey,MetricsT,ProcessT>>
    extends com.sap.sse.landscape.orchestration.Procedure.Builder<BuilderT, CreateLaunchConfigurationAndAutoScalingGroup<ShardingKey, MetricsT, ProcessT>, ShardingKey> {
        void setKeyName(String keyName);

        void setApplicationConfiguration(AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT> applicationConfiguration);

        void setInstanceType(InstanceType instanceType);

        void setImage(AmazonMachineImage<ShardingKey> image);

        void setRelease(Release release);
        
    }
    
    protected static class BuilderImpl<ShardingKey, BuilderT extends Builder<ShardingKey, BuilderT, MetricsT, ProcessT>, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey,MetricsT,ProcessT>>
    extends AbstractProcedureImpl.BuilderImpl<BuilderT, CreateLaunchConfigurationAndAutoScalingGroup<ShardingKey, MetricsT, ProcessT>, ShardingKey>
    implements Builder<ShardingKey, BuilderT, MetricsT, ProcessT> {
        private final ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> applicationReplicaSet;
        private final String replicationBearerToken;
        private final TargetGroup<ShardingKey> targetGroup;
        private final Region region;
        private String keyName;
        private Release release;
        private AmazonMachineImage<ShardingKey> image;
        private InstanceType instanceType;
        private AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT> applicationConfiguration;

        public BuilderImpl(AwsLandscape<ShardingKey> landscape, Region region, ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> applicationReplicaSet,
                String replicationBearerToken, TargetGroup<ShardingKey> targetGroup) {
            this.applicationReplicaSet = applicationReplicaSet;
            this.replicationBearerToken = replicationBearerToken;
            this.targetGroup = targetGroup;
            this.region = region;
            setLandscape(landscape);
        }
        
        @Override
        public void setKeyName(String keyName) {
            this.keyName = keyName;
        }

        @Override
        public void setRelease(Release release) {
            this.release = release;
        }

        @Override
        public void setImage(AmazonMachineImage<ShardingKey> image) {
            this.image = image;
        }

        @Override
        public void setInstanceType(InstanceType instanceType) {
            this.instanceType = instanceType;
        }

        @Override
        public void setApplicationConfiguration(
                AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT> applicationConfiguration) {
            this.applicationConfiguration = applicationConfiguration;
        }

        protected ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> getApplicationReplicaSet() {
            return applicationReplicaSet;
        }

        protected String getReplicationBearerToken() {
            return replicationBearerToken;
        }

        protected TargetGroup<ShardingKey> getTargetGroup() {
            return targetGroup;
        }

        protected Region getRegion() {
            return region;
        }

        protected String getKeyName() {
            return keyName;
        }

        protected Release getRelease() {
            return release;
        }

        protected AmazonMachineImage<ShardingKey> getImage() {
            return image;
        }

        @Override
        public CreateLaunchConfigurationAndAutoScalingGroup<ShardingKey, MetricsT, ProcessT> build() throws Exception {
            return new CreateLaunchConfigurationAndAutoScalingGroup<>(this);
        }

        protected InstanceType getInstanceType() {
            return instanceType;
        }

        protected AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT> getApplicationConfiguration() {
            return applicationConfiguration;
        }
    }
    
    private final ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> applicationReplicaSet;
    private final String replicationBearerToken;
    private final TargetGroup<ShardingKey> targetGroup;
    private final Region region;
    private final String keyName;
    private final Release release;
    private final AmazonMachineImage<ShardingKey> image;
    private final InstanceType instanceType;
    private final AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT> applicationConfiguration;
    
    public static <ShardingKey, BuilderT extends Builder<ShardingKey, BuilderT, MetricsT, ProcessT>, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey,MetricsT,ProcessT>>
    Builder<ShardingKey, BuilderT, MetricsT, ProcessT> builder(
            AwsLandscape<ShardingKey> landscape, Region region,
            ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> applicationReplicaSet,
            String replicationBearerToken, TargetGroup<ShardingKey> targetGroup) {
        return new BuilderImpl<>(landscape, region, applicationReplicaSet, replicationBearerToken, targetGroup);
    }
    
    protected <BuilderT extends Builder<ShardingKey, BuilderT, MetricsT, ProcessT>>
    CreateLaunchConfigurationAndAutoScalingGroup(BuilderImpl<ShardingKey, BuilderT, MetricsT, ProcessT> builder) {
        super(builder);
        this.region = builder.getRegion();
        this.applicationReplicaSet = builder.getApplicationReplicaSet();
        this.replicationBearerToken = builder.getReplicationBearerToken();
        this.targetGroup = builder.getTargetGroup();
        this.release = builder.getRelease();
        this.image = builder.getImage();
        this.keyName = builder.getKeyName();
        this.instanceType = builder.getInstanceType();
        this.applicationConfiguration = builder.getApplicationConfiguration();
    }
    
    @Override
    public AwsLandscape<ShardingKey> getLandscape() {
        return (AwsLandscape<ShardingKey>) super.getLandscape();
    }

    @Override
    public void run() throws Exception {
        getLandscape().createLaunchConfiguration(region, applicationReplicaSet.getMaster(), targetGroup,
                replicationBearerToken, keyName, release, image, instanceType, applicationConfiguration);
    }
}
