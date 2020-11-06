package com.sap.sse.landscape.orchestration;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;

public abstract class StartHost<ShardingKey,
                       MetricsT extends ApplicationProcessMetrics,
                       MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                       ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                       HostT extends Host>
extends AbstractProcedureImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>
implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    /**
     * The {@link AwsLandscape#getLatestImageWithTag(com.sap.sse.landscape.Region, String, String)} method is
     * used to obtain default images for specific AWS host starting procedures that subclass this class. The
     * Amazon Machine Images (AMIs) for this are then expected to be tagged with a tag named as specified by this
     * constant ("image-type"). The tag value then must match what the subclass wants.
     * 
     * @see #getLatestImageOfType(String)
     */
    protected final static String IMAGE_TYPE_TAG_NAME = "image-type";

    private final MachineImage machineImage;
    
    /**
     * A builder that helps building an instance of type {@link StartHost} or any subclass thereof (then using
     * specialized builders).
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<T extends StartHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends Host> {
        T build();
    }
    
    protected abstract static class BuilderImpl<T extends StartHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends Host>
    implements Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        private MachineImage machineImage;
        private Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape;
        private Region region;
        private String imageType;
        
        protected MachineImage getMachineImage() {
            return machineImage == null ? getLandscape().getLatestImageWithTag(getRegion(), IMAGE_TYPE_TAG_NAME, getImageType()) : machineImage;
        }

        protected Region getRegion() {
            return region;
        }

        protected Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setRegion(Region region) {
            this.region = region;
            return this;
        }

        protected Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setMachineImage(MachineImage machineImage) {
            this.machineImage = machineImage;
            return this;
        }
        
        protected String getImageType() {
            return imageType;
        }

        protected Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setImageType(String imageType) {
            this.imageType = imageType;
            return this;
        }

        protected Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
            return landscape;
        }

        protected Builder<T, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setLandscape(Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
            this.landscape = landscape;
            return this;
        }
    }
    
    protected StartHost(BuilderImpl<? extends StartHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder) {
        super(builder.getLandscape());
        this.machineImage = builder.getMachineImage();
    }

    protected MachineImage getMachineImage() {
        return machineImage;
    }
}
