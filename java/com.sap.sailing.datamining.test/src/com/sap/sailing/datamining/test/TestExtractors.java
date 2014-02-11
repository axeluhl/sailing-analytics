package com.sap.sailing.datamining.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.datamining.ExtractionWorker;
import com.sap.sailing.datamining.shared.GenericGroupKey;
import com.sap.sailing.datamining.shared.GroupKey;
import com.sap.sailing.datamining.test.util.OpenDataReceiver;
import com.sap.sailing.datamining.test.util.StringLengthExtractor;

public class TestExtractors {

    @Test
    public void testAbstractExtractor() {
        ExtractionWorker<String, Integer> lengthExtractor = new StringLengthExtractor();
        OpenDataReceiver<Map<GroupKey, Collection<Integer>>> receiver = new OpenDataReceiver<Map<GroupKey, Collection<Integer>>>();
        lengthExtractor.setReceiver(receiver);
        
        Collection<String> dataEntries = Arrays.asList("Fu", "Bar", "Blub");
        Map<GroupKey, Collection<String>> data = new HashMap<GroupKey, Collection<String>>();
        data.put(new GenericGroupKey<Integer>(100), dataEntries);
        lengthExtractor.setDataToExtractFrom(data);
        

        Collection<Integer> expectedExtractedDataEntries = Arrays.asList(2, 3, 4);
        Map<GroupKey, Collection<Integer>> expectedExtractedData = new HashMap<GroupKey, Collection<Integer>>();
        expectedExtractedData.put(new GenericGroupKey<Integer>(100), expectedExtractedDataEntries);
        lengthExtractor.run();
        assertEquals(expectedExtractedData, receiver.result);
    }

}
