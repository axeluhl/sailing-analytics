package com.sap.sse.landscape.orchestration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;

public class ProcedureSequence<ShardingKey, 
MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends AbstractProcedureImpl<ShardingKey,MetricsT,ProcessT> 
implements Procedure<ShardingKey, MetricsT, ProcessT>, Iterable<Procedure<ShardingKey, MetricsT, ProcessT>> {
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>,
    T extends ProcedureSequence<ShardingKey, MetricsT, ProcessT>, ShardingKey, 
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends Procedure.Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
        /**
         * An alias for {@link #then(com.sap.sse.landscape.orchestration.Procedure.Builder)} which makes writing
         * a procedure sequence a bit more human-readable.
         */
        default BuilderT first(Procedure.Builder<?, ?, ShardingKey, MetricsT, ProcessT> firstStepBuilder) {
            return then(firstStepBuilder);
        }
        
        /**
         * When building the procedure sequence, the next step is built with {@code stepBuilder}.
         */
        BuilderT then(Procedure.Builder<?, ?, ShardingKey, MetricsT, ProcessT> stepBuilder);
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ProcedureSequence<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>,
    ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends AbstractProcedureImpl.BuilderImpl<BuilderT, ProcedureSequence<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>
    implements Builder<BuilderT, ProcedureSequence<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> {
        final List<Procedure.Builder<?, ?, ShardingKey, MetricsT, ProcessT>> stepBuilders = new ArrayList<>();

        @Override
        public ProcedureSequence<ShardingKey, MetricsT, ProcessT> build() throws Exception {
            return new ProcedureSequence<>(this);
        }

        @Override
        public BuilderT then(
                com.sap.sse.landscape.orchestration.Procedure.Builder<?, ?, ShardingKey, MetricsT, ProcessT> stepBuilder) {
            stepBuilders.add(stepBuilder);
            return self();
        }

        protected Iterable<Procedure.Builder<?, ?, ShardingKey, MetricsT, ProcessT>> getStepBuilders() {
            return stepBuilders;
        }
    }

    private final Iterable<Procedure<ShardingKey, MetricsT, ProcessT>> steps;
    
    public static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    BuilderT extends Builder<BuilderT, ProcedureSequence<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>>
    Builder<BuilderT, ProcedureSequence<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT>();
    }
    
    protected ProcedureSequence(BuilderImpl<?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
        final List<Procedure<ShardingKey, MetricsT, ProcessT>> mySteps = new ArrayList<>();
        for (final com.sap.sse.landscape.orchestration.Procedure.Builder<?, ?, ShardingKey, MetricsT, ProcessT> stepBuilder : builder.getStepBuilders()) {
            mySteps.add(stepBuilder.build());
        }
        this.steps = Collections.unmodifiableList(mySteps);
    }

    @Override
    public void run() throws Exception {
        for (final Procedure<ShardingKey, MetricsT, ProcessT> s : this) {
            s.run();
        }
    }

    @Override
    public Iterator<Procedure<ShardingKey, MetricsT, ProcessT>> iterator() {
        return steps.iterator();
    }
}
