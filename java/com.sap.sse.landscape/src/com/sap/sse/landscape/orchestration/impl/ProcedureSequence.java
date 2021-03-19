package com.sap.sse.landscape.orchestration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;

public class ProcedureSequence<ShardingKey>
extends AbstractProcedureImpl<ShardingKey> 
implements Procedure<ShardingKey>, Iterable<Procedure<ShardingKey>> {
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends ProcedureSequence<ShardingKey>, ShardingKey>
    extends Procedure.Builder<BuilderT, T, ShardingKey> {
        /**
         * An alias for {@link #then(com.sap.sse.landscape.orchestration.Procedure.Builder)} which makes writing
         * a procedure sequence a bit more human-readable.
         */
        default BuilderT first(Procedure.Builder<?, ?, ShardingKey> firstStepBuilder) {
            return then(firstStepBuilder);
        }
        
        /**
         * When building the procedure sequence, the next step is built with {@code stepBuilder}.
         */
        BuilderT then(Procedure.Builder<?, ?, ShardingKey> stepBuilder);
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ProcedureSequence<ShardingKey>, ShardingKey>, ShardingKey>
    extends AbstractProcedureImpl.BuilderImpl<BuilderT, ProcedureSequence<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ProcedureSequence<ShardingKey>, ShardingKey> {
        final List<Procedure.Builder<?, ?, ShardingKey>> stepBuilders = new ArrayList<>();

        @Override
        public ProcedureSequence<ShardingKey> build() throws Exception {
            return new ProcedureSequence<>(this);
        }

        @Override
        public BuilderT then(
                com.sap.sse.landscape.orchestration.Procedure.Builder<?, ?, ShardingKey> stepBuilder) {
            stepBuilders.add(stepBuilder);
            return self();
        }

        protected Iterable<Procedure.Builder<?, ?, ShardingKey>> getStepBuilders() {
            return stepBuilders;
        }
    }

    private final Iterable<Procedure<ShardingKey>> steps;
    
    public static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    BuilderT extends Builder<BuilderT, ProcedureSequence<ShardingKey>, ShardingKey>>
    Builder<BuilderT, ProcedureSequence<ShardingKey>, ShardingKey> builder() {
        return new BuilderImpl<BuilderT, ShardingKey>();
    }
    
    protected ProcedureSequence(BuilderImpl<?, ShardingKey> builder) throws Exception {
        super(builder);
        final List<Procedure<ShardingKey>> mySteps = new ArrayList<>();
        for (final com.sap.sse.landscape.orchestration.Procedure.Builder<?, ?, ShardingKey> stepBuilder : builder.getStepBuilders()) {
            mySteps.add(stepBuilder.build());
        }
        this.steps = Collections.unmodifiableList(mySteps);
    }

    @Override
    public void run() throws Exception {
        for (final Procedure<ShardingKey> s : this) {
            s.run();
        }
    }

    @Override
    public Iterator<Procedure<ShardingKey>> iterator() {
        return steps.iterator();
    }
}
