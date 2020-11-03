package com.sap.sailing.landscape.procedures;

import java.util.Optional;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * TODO handle the naming problem: base name drives instance "Name" tag generation ("SL ... (Master)"), exchange name,
 * database name and SERVER_NAME. When moving up the inheritance hierarchy, name is interpreted in some places as the instance name
 * which obviously doesn't equal the "Name" tag value. So, we have to clearly distinguish these.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 */
public abstract class StartSailingAnalyticsHost<ShardingKey, 
                                                HostT extends SailingAnalyticsHost<ShardingKey>>
extends StartAwsHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, HostT>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    private final static String IMAGE_TYPE_TAG_VALUE_SAILING = "sailing-analytics-server";
    private final static String INSTANCE_NAME_DEFAULT_PREFIX = "SL ";

    /**
     * The following defaults, in addition to the defaults implemented by the more general {@link StartAwsHost.Builder},
     * are:
     * <ul>
     * <li>If no {@link #setInstanceName(String) instance name} is provided, the instance name is constructed from the {@link #getServerName() server name}
     * by pre-pending the prefix "SL ".</li>
     * <li>Uses the latest machine image of the type described by
     * {@link StartSailingAnalyticsHost#IMAGE_TYPE_TAG_VALUE_SAILING} if no explicit {@link #setMachineImage(AmazonMachineImage) machine image is set}
     * and no {@link #setImageType(String) image type is set} of which the latest version would be used otherwise.</li>
     * <li>If no {@link Release} is explicitly {@link #setRelease set}, or that {@link Optional} is empty,
     * {@link SailingReleaseRepository#INSTANCE}{@link SailingReleaseRepository#getLatestMasterRelease()
     * getLatestMasterRelease()} will be used instead.</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<T extends StartSailingAnalyticsHost<ShardingKey, HostT>, ShardingKey, HostT extends SailingAnalyticsHost<ShardingKey>>
    extends StartAwsHost.Builder<T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, HostT> {
    }
    
    protected abstract static class BuilderImpl<T extends StartSailingAnalyticsHost<ShardingKey, HostT>, ShardingKey, HostT extends SailingAnalyticsHost<ShardingKey>>
    extends StartAwsHost.BuilderImpl<T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, HostT>
    implements Builder<T, ShardingKey, HostT> {
        @Override
        public String getImageType() {
            return super.getImageType() == null ? StartSailingAnalyticsHost.IMAGE_TYPE_TAG_VALUE_SAILING : super.getImageType();
        }

        @Override
        public Optional<Release> getRelease() {
            return Optional.of(super.getRelease().orElse(SailingReleaseRepository.INSTANCE.getLatestMasterRelease()));
        }

        @Override
        public String getInstanceName() {
            return super.getInstanceName() == null ? INSTANCE_NAME_DEFAULT_PREFIX+getServerName() : super.getInstanceName();
        }
    }
    
    protected StartSailingAnalyticsHost(Builder<? extends StartSailingAnalyticsHost<ShardingKey,HostT>, ShardingKey, HostT> builder) {
        super(builder);
    }
}
