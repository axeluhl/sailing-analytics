package com.sap.sse.datamining.impl.components;

public class ProcessorInstructionPriority {

    public static final ProcessorInstructionPriority Aggregation = new ProcessorInstructionPriority(0);
    public static final ProcessorInstructionPriority Extraction = new ProcessorInstructionPriority(1);
    public static final ProcessorInstructionPriority Filtration = new ProcessorInstructionPriority(1);
    public static final ProcessorInstructionPriority Grouping = new ProcessorInstructionPriority(2);
    
    private static final int retrievalBasePriority = Integer.MAX_VALUE;
    
    /**
     * Creates a priority for the given <code>retrieverLevel</code>, where the level <code>0</code>
     * represents the first retriever in the chain.
     * 
     * @param retrievalLevel The position of the retriever in its chain
     * @return A priority for the given <code>retrieverLevel</code>
     */
    public static ProcessorInstructionPriority createRetrievalPriority(int retrievalLevel) {
        return new ProcessorInstructionPriority(retrievalBasePriority - retrievalLevel);
    }

    private final int intValue;

    private ProcessorInstructionPriority(int intValue) {
        this.intValue = intValue;
    }

    public int asIntValue() {
        return intValue;
    }

}
