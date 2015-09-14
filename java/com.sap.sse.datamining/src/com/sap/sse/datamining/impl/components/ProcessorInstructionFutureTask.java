package com.sap.sse.datamining.impl.components;

import java.util.concurrent.FutureTask;

import com.sap.sse.datamining.components.ProcessorInstruction;

public class ProcessorInstructionFutureTask<ResultType> extends FutureTask<ResultType> implements Comparable<ProcessorInstructionFutureTask<?>> {

    private final ProcessorInstruction<ResultType> instruction;

    public ProcessorInstructionFutureTask(ProcessorInstruction<ResultType> instruction) {
        super(instruction);
        this.instruction = instruction;
    }

    @Override
    public int compareTo(ProcessorInstructionFutureTask<?> other) {
        return instruction.compareTo(other.instruction);
    }

}
