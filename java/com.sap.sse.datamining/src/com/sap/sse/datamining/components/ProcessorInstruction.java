package com.sap.sse.datamining.components;

public interface ProcessorInstruction<ResultType> extends Runnable, Comparable<ProcessorInstruction<?>> {

    public int getPriority();

}
