package com.sap.sailing.barbados.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.barbados.resultimport.impl.BarbadosResultSpreadsheet;
import com.sap.sailing.barbados.resultimport.impl.ScoreCorrectionProviderImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.resultimport.ResultDocumentProvider;

public class BarbadosResultImportTest {
    private static final String SAMPLE_INPUT_NAME_EMPTY_RESULTS = "RESULTS-505Barbados.xlsx";
    private static final String RESOURCES = "resources/";
    private BarbadosResultSpreadsheet spreadsheet;

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

    @Before
    public void setUp() throws FileNotFoundException, IOException, Exception {
        spreadsheet = new BarbadosResultSpreadsheet(getInputStream(SAMPLE_INPUT_NAME_EMPTY_RESULTS));
    }
    
    @Test
    public void testOpenDocument() {
        assertNotNull(spreadsheet);
        RegattaResults regattaResults = spreadsheet.getRegattaResults();
        assertNotNull(regattaResults);
        assertEquals("505", regattaResults.getMetadata().get(ScoreCorrectionProviderImpl.BOATCLASS_NAME_METADATA_PROPERTY));
    }
    
    @Test
    public void testResultsThroughScoreCorrectionProvider() throws Exception {
        ScoreCorrectionProviderImpl scp = new ScoreCorrectionProviderImpl(new ResultDocumentProvider() {
            @Override
            public Iterable<Triple<InputStream, String, TimePoint>> getDocumentsAndNamesAndLastModified() throws IOException {
                return Collections.singleton(new Triple<InputStream, String, TimePoint>(getInputStream(SAMPLE_INPUT_NAME_EMPTY_RESULTS),
                        SAMPLE_INPUT_NAME_EMPTY_RESULTS, MillisecondsTimePoint.now()));
            }
        });
        Map<String, Set<Pair<String, TimePoint>>> hasResultsFor = scp.getHasResultsForBoatClassFromDateByEventName();
        assertNotNull(hasResultsFor);
        assertEquals(1, hasResultsFor.size());
        Entry<String, Set<Pair<String, TimePoint>>> entry = hasResultsFor.entrySet().iterator().next();
        assertEquals("505", entry.getKey());
    }
}
