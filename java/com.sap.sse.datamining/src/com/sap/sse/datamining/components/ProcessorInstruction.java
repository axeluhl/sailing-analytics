package com.sap.sse.datamining.components;

import java.util.concurrent.Callable;

public interface ProcessorInstruction<ResultType> extends Callable<ResultType>, Comparable<ProcessorInstruction<?>> {

    public int getPriority();

}
