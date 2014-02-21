package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractSimpleParallelProcessor<InputType, ResultType>
		extends
		AbstractPartitioningParallelProcessor<InputType, InputType, ResultType> {

	public AbstractSimpleParallelProcessor(Executor executor,
			Collection<Processor<ResultType>> resultReceivers) {
		super(executor, resultReceivers);
	}

	@Override
	protected Iterable<InputType> partitionElement(InputType element) {
		return Collections.singleton(element);
	}

}
