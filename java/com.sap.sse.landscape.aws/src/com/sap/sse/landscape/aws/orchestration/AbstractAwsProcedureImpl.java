package com.sap.sse.landscape.aws.orchestration;

import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;

public abstract class AbstractAwsProcedureImpl<ShardingKey,
MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends AbstractProcedureImpl<ShardingKey, MetricsT, ProcessT> {
    public static interface Builder<BuilderT extends com.sap.sse.common.Builder<BuilderT, T>,
    T extends Procedure<ShardingKey, MetricsT, ProcessT>, ShardingKey, 
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends Procedure.Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
    }

    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>,
    T extends Procedure<ShardingKey, MetricsT, ProcessT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends AbstractProcedureImpl.BuilderImpl<BuilderT, T, ShardingKey, MetricsT, ProcessT>
    implements Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
        protected AwsLandscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
            return (AwsLandscape<ShardingKey, MetricsT, ProcessT>) super.getLandscape();
        }
    }
    
    public AbstractAwsProcedureImpl(BuilderImpl<?, ?, ShardingKey, MetricsT, ProcessT> builder) {
        super(builder);
    }

    @Override
    public AwsLandscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
        return (AwsLandscape<ShardingKey, MetricsT, ProcessT>) super.getLandscape();
    }
}
