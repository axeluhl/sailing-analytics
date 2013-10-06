package com.sap.sailing.datamining.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.datamining.impl.AbstractExtractor;
import com.sap.sailing.datamining.shared.Unit;

public class TestExtractors {

    @Test
    public void testAbstractExtractor() {
        Collection<String> data = Arrays.asList("Fu", "Bar", "Blub");
        Extractor<String, Integer> lengthExtractor = new LengthExtractor();
        Collection<Integer> expectedExtractedData = Arrays.asList(2, 3, 4);
        assertEquals(expectedExtractedData, lengthExtractor.extract(data));
    }
    
    private class LengthExtractor extends AbstractExtractor<String, Integer> {

		public LengthExtractor() {
			super("", Unit.None, 0);
		}

		@Override
		public Integer extract(String dataEntry) {
			return dataEntry.length();
		}
    	
    }

}
