package com.sap.sailing.datamining.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.datamining.ExtractionWorker;
import com.sap.sailing.datamining.WorkReceiver;
import com.sap.sailing.datamining.impl.AbstractExtractionWorker;
import com.sap.sailing.datamining.shared.GenericGroupKey;
import com.sap.sailing.datamining.shared.GroupKey;

public class TestExtractors {

    @Test
    public void testAbstractExtractor() {
        ExtractionWorker<String, Integer> lengthExtractor = new LengthExtractor();
        DataReceiver receiver = new DataReceiver();
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
    
    private class LengthExtractor extends AbstractExtractionWorker<String, Integer> {

        @Override
        public Integer extract(String dataEntry) {
            return dataEntry.length();
        }

    }
    
    private class DataReceiver implements WorkReceiver<Map<GroupKey, Collection<Integer>>> {

        public Map<GroupKey, Collection<Integer>> result;

        @Override
        public void receiveWork(Map<GroupKey, Collection<Integer>> result) {
            this.result = result;
        }
        
    }

}
