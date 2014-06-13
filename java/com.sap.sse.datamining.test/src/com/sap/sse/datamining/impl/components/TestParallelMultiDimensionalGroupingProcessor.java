package com.sap.sse.datamining.impl.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.functions.MethodWrappingFunction;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.components.util.Number;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestParallelMultiDimensionalGroupingProcessor {
    
    private Processor<Number> processor;
    private Collection<Processor<GroupedDataEntry<Number>>> receivers;

    private GroupedDataEntry<Number> groupedElement;

    @Before
    public void intializeProcessor() throws IllegalArgumentException, NoSuchMethodException, SecurityException {
        Processor<GroupedDataEntry<Number>> receiver = new Processor<GroupedDataEntry<Number>>() {
            @Override
            public void processElement(GroupedDataEntry<Number> element) {
                groupedElement = element;
            }
            @Override
            public void onFailure(Throwable failure) { }
            @Override
            public void finish() throws InterruptedException { }
            @Override
            public void abort() { }
            @Override
            public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
                return additionalDataBuilder;
            }
        };
        
        receivers = new ArrayList<>();
        receivers.add(receiver);
        
        Collection<Function<?>> dimensions = new ArrayList<Function<?>>();
        dimensions.add(new MethodWrappingFunction<>(Number.class.getMethod("getLength", new Class<?>[0]), int.class));
        dimensions.add(new MethodWrappingFunction<>(Number.class.getMethod("getCrossSum", new Class<?>[0]), int.class));
        
        processor = new ParallelMultiDimensionalGroupingProcessor<Number>(ConcurrencyTestsUtil.getExecutor(), receivers, dimensions);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructionWithNullDimensions() {
        new ParallelMultiDimensionalGroupingProcessor<>(ConcurrencyTestsUtil.getExecutor(), receivers, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructionWithEmptyDimensions() {
        Iterable<Function<?>> dimensions = new ArrayList<>();
        new ParallelMultiDimensionalGroupingProcessor<>(ConcurrencyTestsUtil.getExecutor(), receivers, dimensions);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructionWithFunctionsInsteadOfDimensions() {
        Collection<Function<?>> functions = new ArrayList<>();
        Method method = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("sideEffectFreeValue");
        functions.add(FunctionFactory.createMethodWrappingFunction(method));
        new ParallelMultiDimensionalGroupingProcessor<>(ConcurrencyTestsUtil.getExecutor(), receivers, functions);
    }

    @Test
    public void testGroupKeyGeneration() {
        Number number = new Number(1111);
        processor.processElement(number);
        ConcurrencyTestsUtil.sleepFor(200); //Giving the processor time to finish the instruction
        verifyGroupedElement(number);
    }

    private void verifyGroupedElement(Number originElement) {
        GroupKey mainKey = new GenericGroupKey<Object>(originElement.getLength());
        GroupKey subKey = new GenericGroupKey<Object>(originElement.getCrossSum());
        GroupKey expectedKey = new CompoundGroupKey(mainKey, subKey);
        assertThat(groupedElement.getKey(), is(expectedKey));
    }

}
