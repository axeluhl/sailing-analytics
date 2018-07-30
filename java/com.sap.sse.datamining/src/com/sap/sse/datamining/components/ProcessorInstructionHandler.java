package com.sap.sse.datamining.components;

public interface ProcessorInstructionHandler<ResultType> {
    
    boolean isAborted();
    
    void instructionSucceeded(ResultType result);
    void instructionFailed(Exception e);
    
    void afterInstructionFinished(ProcessorInstruction<ResultType> instruction);

}
