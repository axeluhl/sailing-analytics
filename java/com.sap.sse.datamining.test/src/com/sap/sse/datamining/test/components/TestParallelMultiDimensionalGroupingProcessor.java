package com.sap.sse.datamining.test.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelMultiDimensionalGroupingProcessor;
import com.sap.sse.datamining.impl.functions.MethodWrappingFunction;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.components.util.Number;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;

public class TestParallelMultiDimensionalGroupingProcessor {
    
    private Processor<Iterable<Number>> processor;

    private GroupedDataEntry<Number> groupedElement;

    @Before
    public void intializeProcessor() throws IllegalArgumentException, NoSuchMethodException, SecurityException {
        Processor<GroupedDataEntry<Number>> receiver = new Processor<GroupedDataEntry<Number>>() {
            @Override
            public void onElement(GroupedDataEntry<Number> element) {
                groupedElement = element;
            }
            @Override
            public void finish() throws InterruptedException { }
        };
        
        Collection<Processor<GroupedDataEntry<Number>>> receivers = new ArrayList<>();
        receivers.add(receiver);
        Collection<Function<?>> dimensions = new ArrayList<Function<?>>();
        dimensions.add(new MethodWrappingFunction<>(Number.class.getMethod("getLength", new Class<?>[0]), int.class));
        dimensions.add(new MethodWrappingFunction<>(Number.class.getMethod("getCrossSum", new Class<?>[0]), int.class));
        processor = new ParallelMultiDimensionalGroupingProcessor<Number>(ConcurrencyTestsUtil.getExecutor(), receivers, dimensions);
    }

    @Test
    public void testGroupKeyGeneration() {
        Number number = new Number(1111);
        processor.onElement(Arrays.asList(number));
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
