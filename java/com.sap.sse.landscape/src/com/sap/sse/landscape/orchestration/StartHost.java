package com.sap.sse.landscape.orchestration;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

public abstract class StartHost<ShardingKey,
                       MetricsT extends ApplicationProcessMetrics,
                       ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
                       HostT extends Host>
extends AbstractProcedureImpl<ShardingKey, MetricsT, ProcessT>
implements Procedure<ShardingKey, MetricsT, ProcessT> {
    private final MachineImage machineImage;
    
    /**
     * A builder that helps building an instance of type {@link StartHost} or any subclass thereof (then using
     * specialized builders).
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends StartHost<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    HostT extends Host>
    extends Procedure.Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
        BuilderT setImageType(String imageType);
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends StartHost<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    HostT extends Host>
    extends AbstractProcedureImpl.BuilderImpl<BuilderT, T, ShardingKey, MetricsT, ProcessT>
    implements Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT> {
        private MachineImage machineImage;
        private Region region;
        private String imageType;
        
        protected MachineImage getMachineImage() {
            return machineImage == null ? getLandscape().getLatestImageWithType(getRegion(), getImageType()) : machineImage;
        }

        protected Region getRegion() {
            return region;
        }

        protected BuilderT setRegion(Region region) {
            this.region = region;
            return self();
        }

        protected BuilderT setMachineImage(MachineImage machineImage) {
            this.machineImage = machineImage;
            return self();
        }
        
        protected String getImageType() {
            return imageType;
        }

        @Override
        public BuilderT setImageType(String imageType) {
            this.imageType = imageType;
            return self();
        }
    }
    
    protected StartHost(BuilderImpl<?, ? extends StartHost<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey, MetricsT, ProcessT, HostT> builder) {
        super(builder);
        this.machineImage = builder.getMachineImage();
    }

    protected MachineImage getMachineImage() {
        return machineImage;
    }
}
