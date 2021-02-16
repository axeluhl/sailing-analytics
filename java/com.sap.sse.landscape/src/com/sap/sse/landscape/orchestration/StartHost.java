package com.sap.sse.landscape.orchestration;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Region;

public abstract class StartHost<ShardingKey, HostT extends Host>
extends AbstractProcedureImpl<ShardingKey>
implements Procedure<ShardingKey> {
    private final MachineImage machineImage;
    
    /**
     * A builder that helps building an instance of type {@link StartHost} or any subclass thereof (then using
     * specialized builders). The following default rules apply:
     * <ul>
     * <li>If no explicit {@link #setMachineImage(MachineImage) machine image} is specified, an {@link #setImageType(String) image type}
     * is expected which is then used to look up the latest image of that type in the region that needs to also be specified by the
     * concrete builder implementation.</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, HostT>,
    T extends StartHost<ShardingKey, HostT>, ShardingKey,
    HostT extends Host>
    extends Procedure.Builder<BuilderT, T, ShardingKey> {
        BuilderT setImageType(String imageType);
        BuilderT setMachineImage(MachineImage machineImage);
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, HostT>,
    T extends StartHost<ShardingKey, HostT>, ShardingKey,
    HostT extends Host>
    extends AbstractProcedureImpl.BuilderImpl<BuilderT, T, ShardingKey>
    implements Builder<BuilderT, T, ShardingKey, HostT> {
        private MachineImage machineImage;
        private String imageType;
        
        protected MachineImage getMachineImage() {
            return machineImage == null ? getLandscape().getLatestImageWithType(getRegion(), getImageType()) : machineImage;
        }

        protected abstract Region getRegion();

        @Override
        public BuilderT setMachineImage(MachineImage machineImage) {
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
    
    protected StartHost(BuilderImpl<?, ? extends StartHost<ShardingKey,HostT>, ShardingKey, HostT> builder) {
        super(builder);
        this.machineImage = builder.getMachineImage();
    }

    protected MachineImage getMachineImage() {
        return machineImage;
    }
}
