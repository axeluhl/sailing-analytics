package com.sap.sailing.datamining.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.datamining.impl.AbstractExtractor;

public class TestExtractors {

    @Test
    public void testAbstractExtractor() {
        Collection<String> data = Arrays.asList("Fu", "Bar", "Blub");
        Extractor<String, Integer> lengthExtractor = new AbstractExtractor<String, Integer>() {
            @Override
            public Integer extract(String dataEntry) {
                return dataEntry.length();
            }
        };
        Collection<Integer> expectedExtractedData = Arrays.asList(2, 3, 4);
        assertEquals(expectedExtractedData, lengthExtractor.extract(data));
    }

}
