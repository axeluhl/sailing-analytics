package com.sap.sse.landscape.aws.orchestration;

import java.util.Collections;

import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * Uses an existing Amazon Machine Image that is expected to not start any application processes after booting. Can be
 * used to do specific work that requires any default application start-up not to happen.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <HostT>
 */
public abstract class StartEmptyServer<T extends StartEmptyServer<T, ShardingKey, HostT>,
ShardingKey, HostT extends AwsInstance<ShardingKey>>
extends StartAwsHost<ShardingKey, HostT>
implements Procedure<ShardingKey> {
    private static final String NO_SHUTDOWN_USER_DATA = "no-shutdown";
    
    /**
     * Additional default rules in addition to what the {@link StartAwsHost.Builder parent builder} defines:
     * 
     * <ul>
     * <li>If no {@link #getInstanceName() instance name} is set, the default instance name will be constructed as
     * {@code IMAGE_UPGRADE+" for "+machineImage.getId()}</li>
     * <li>The {@link #isNoShutdown()} defaults to {@code true} because "starting" a server is what the {@link StartEmptyServer}
     * procedure is meant to do. Specialized builders may change this default.</li>
     * <li>The user data are set to the string defined by {@link StartEmptyServer#IMAGE_UPGRADE_USER_DATA}, forcing the image to
     * boot without trying to launch a process instance.</li>
     * </ul>
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, HostT>,
    T extends StartEmptyServer<T, ShardingKey, HostT>,
    ShardingKey, HostT extends AwsInstance<ShardingKey>>
    extends StartAwsHost.Builder<BuilderT, T, ShardingKey, HostT> {
        BuilderT setNoShutdown(boolean noShutdown);
    }

    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, HostT>,
    T extends StartEmptyServer<T, ShardingKey, HostT>,
    ShardingKey, HostT extends AwsInstance<ShardingKey>>
    extends StartAwsHost.BuilderImpl<BuilderT, T, ShardingKey, HostT>
    implements Builder<BuilderT, T, ShardingKey, HostT> {
        private boolean noShutdown = false;
        
        protected boolean isNoShutdown() {
            return noShutdown;
        }

        @Override
        public BuilderT setNoShutdown(boolean noShutdown) {
            this.noShutdown = noShutdown;
            return self();
        }
    }
    
    public StartEmptyServer(BuilderImpl<?, T, ShardingKey, HostT> builder) {
        super(builder);
        if (builder.isNoShutdown()) {
            addUserData(Collections.singleton(NO_SHUTDOWN_USER_DATA));
        }
    }
}
