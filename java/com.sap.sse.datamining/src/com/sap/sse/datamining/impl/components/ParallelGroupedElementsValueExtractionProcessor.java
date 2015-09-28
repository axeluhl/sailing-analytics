package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.components.ProcessorInstruction;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;

/**
 * These Processors are used to extract a value (defined by a {@link Function}) from grouped elements. 
 * 
 * @author Lennart Hensler (D054527)
 *
 * @param <DataType> The type of the grouped data. The <code>InputType</code> of the Processor will be
 *                   <code>GroupedDataEntry<DataType></code>.
 * @param <FunctionReturnType> The return type of the function, that is used to extract the value. The
 *                             <code>ResultType</code> of the Processor will be <code>GroupedDataEntry<FunctionReturnType>></code>.
 * 
 * @see com.sap.sse.datamining.impl.components com.sap.sse.datamining.impl.components for general information about Processors.
 */
public class ParallelGroupedElementsValueExtractionProcessor<DataType, FunctionReturnType>
             extends AbstractParallelProcessor<GroupedDataEntry<DataType>, GroupedDataEntry<FunctionReturnType>> {

    private final Function<FunctionReturnType> extractionFunction;
    private final ParameterProvider parameterProvider;
    
    public ParallelGroupedElementsValueExtractionProcessor(ExecutorService executor,
            Collection<Processor<GroupedDataEntry<FunctionReturnType>, ?>> resultReceivers,
            Function<FunctionReturnType> extractionFunction) {
        this(executor, resultReceivers, extractionFunction, ParameterProvider.NULL);
    }
    
    @SuppressWarnings("unchecked")
    public ParallelGroupedElementsValueExtractionProcessor(ExecutorService executor,
                                                           Collection<Processor<GroupedDataEntry<FunctionReturnType>, ?>> resultReceivers,
                                                           Function<FunctionReturnType> extractionFunction, ParameterProvider parameterProvider) {
        super((Class<GroupedDataEntry<DataType>>)(Class<?>) GroupedDataEntry.class, (Class<GroupedDataEntry<FunctionReturnType>>)(Class<?>) GroupedDataEntry.class, executor, resultReceivers);
        this.extractionFunction = extractionFunction;
        this.parameterProvider = parameterProvider;
    }

    @Override
    protected ProcessorInstruction<GroupedDataEntry<FunctionReturnType>> createInstruction(final GroupedDataEntry<DataType> element) {
        return new AbstractProcessorInstruction<GroupedDataEntry<FunctionReturnType>>(this, ProcessorInstructionPriority.Extraction) {
            @Override
            public GroupedDataEntry<FunctionReturnType> computeResult() {
                FunctionReturnType value = extractionFunction.tryToInvoke(element.getDataEntry(), parameterProvider);
                return value != null ? new GroupedDataEntry<FunctionReturnType>(element.getKey(), value) :
                                       ParallelGroupedElementsValueExtractionProcessor.super.createInvalidResult();
            }
        };
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        additionalDataBuilder.setExtractionFunction(extractionFunction);
    }

}
