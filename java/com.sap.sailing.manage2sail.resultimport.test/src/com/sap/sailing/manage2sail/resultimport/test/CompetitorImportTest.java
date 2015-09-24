package com.sap.sailing.manage2sail.resultimport.test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.sap.sailing.manage2sail.resultimport.AbstractManage2SailProvider;
import com.sap.sailing.manage2sail.resultimport.CompetitorDescriptor;
import com.sap.sailing.manage2sail.resultimport.CompetitorImporter;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.xrr.resultimport.ParserFactory;

public class CompetitorImportTest extends AbstractEventResultJsonServiceTest {
    @Test
    public void simpleCompetitorImportTest() throws FileNotFoundException, IOException, JAXBException {
        ResultUrlRegistry resultUrlRegistry = mock(ResultUrlRegistry.class);
        when(resultUrlRegistry.getResultUrls(AbstractManage2SailProvider.NAME)).thenReturn(Arrays.asList(getClass().getClassLoader().getResource(EVENT_RESULTS_JSON)));
        final CompetitorImporter competitorImporter = new CompetitorImporter(ParserFactory.INSTANCE, resultUrlRegistry);
        final Iterable<CompetitorDescriptor> competitorDescriptors = competitorImporter.getCompetitorDescriptors();
        assertNotNull(competitorDescriptors);
    }
}
