package com.sap.sse.landscape.aws.orchestration;

import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;

public abstract class AbstractAwsProcedureImpl<ShardingKey>
extends AbstractProcedureImpl<ShardingKey> {
    public static interface Builder<BuilderT extends com.sap.sse.common.Builder<BuilderT, T>,
    T extends Procedure<ShardingKey>, ShardingKey>
    extends Procedure.Builder<BuilderT, T, ShardingKey> {
    }

    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends Procedure<ShardingKey>, ShardingKey>
    extends AbstractProcedureImpl.BuilderImpl<BuilderT, T, ShardingKey>
    implements Builder<BuilderT, T, ShardingKey> {
        protected AwsLandscape<ShardingKey> getLandscape() {
            return (AwsLandscape<ShardingKey>) super.getLandscape();
        }
    }
    
    public AbstractAwsProcedureImpl(BuilderImpl<?, ?, ShardingKey> builder) {
        super(builder);
    }

    @Override
    public AwsLandscape<ShardingKey> getLandscape() {
        return (AwsLandscape<ShardingKey>) super.getLandscape();
    }
}
