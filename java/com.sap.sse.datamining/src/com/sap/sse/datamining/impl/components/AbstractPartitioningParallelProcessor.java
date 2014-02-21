package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractPartitioningParallelProcessor<InputType, WorkingType, ResultType> implements Processor<InputType> {
	
	private static final Logger LOGGER = Logger.getLogger(AbstractPartitioningParallelProcessor.class.getName());
	
	private Set<Processor<ResultType>> resultReceivers;
	private Executor executor;
	
	private int openInstructions;

	public AbstractPartitioningParallelProcessor(Executor executor, Collection<Processor<ResultType>> resultReceivers) {
		this.executor = executor;
		this.resultReceivers = new HashSet<Processor<ResultType>>(resultReceivers);
	}

	@Override
	public void onElement(InputType element) {
		for (WorkingType partialElement : partitionElement(element)) {
			Runnable instruction = createInstruction(partialElement);
			if (instructionIsValid(instruction)) {
				NotifyingInstruction notifyingInstruction = new NotifyingInstruction(
						instruction);
				openInstructions++;
				executor.execute(notifyingInstruction);
			}
		}
	}

	protected abstract Runnable createInstruction(WorkingType partialElement);

	protected abstract Iterable<WorkingType> partitionElement(InputType element);
	
	private boolean instructionIsValid(Runnable instruction) {
		return instruction != null;
	}

	public void instructionCompleted() {
		openInstructions--;
	}

	protected Set<Processor<ResultType>> getResultReceivers() {
		return resultReceivers;
	}
	
	@Override
	public void finish() throws InterruptedException {
		while (!isDone()) {
			Thread.sleep(100);
		}
		finishResultReceivers();
	}
	
	private boolean isDone() {
		return openInstructions == 0;
	}

	private void finishResultReceivers() {
		for (Processor<ResultType> resultReceiver : getResultReceivers()) {
			try {
				resultReceiver.finish();
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, resultReceiver.toString() + " was interrupted", e);
			}
		}
	}

	private class NotifyingInstruction implements Runnable {
		
		private final Runnable innerInstruction;

		public NotifyingInstruction(Runnable instruction) {
			this.innerInstruction = instruction;
		}

		@Override
		public void run() {
			innerInstruction.run();
			AbstractPartitioningParallelProcessor.this.instructionCompleted();
		}
		
	}
	
}
