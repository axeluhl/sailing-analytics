package com.sap.sse.datamining.components;

public interface ProcessorInstructionHandler<ResultType> {
    
    public void instructionSucceeded(ResultType result);
    public void instructionFailed(Exception e);
    
    public void afterInstructionFinished();

}
