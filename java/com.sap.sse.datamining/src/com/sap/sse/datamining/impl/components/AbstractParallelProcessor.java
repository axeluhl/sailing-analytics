package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.RunnableFuture;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractParallelProcessor<ElementType, ResultType> implements Processor<ElementType> {
	
	private Set<Processor<ResultType>> resultReceivers;
	private Executor executor;

	public AbstractParallelProcessor(Executor executor, Collection<Processor<ResultType>> resultReceivers) {
		this.executor = executor;
		this.resultReceivers = new HashSet<Processor<ResultType>>(resultReceivers);
	}

	@Override
	public void onElement(ElementType element) {
		executor.execute(createFuture(element));
	}
	
	protected abstract RunnableFuture<ResultType> createFuture(ElementType element);

	protected Set<Processor<ResultType>> getResultReceivers() {
		return resultReceivers;
	}
	
	/**
	 * Tells this Processor, that there will be no incoming data.<br />
	 * The called Processor will finish his work and call <code>finish()</code> on all subsequent processors.
	 */
	protected abstract void finish();
	
}
