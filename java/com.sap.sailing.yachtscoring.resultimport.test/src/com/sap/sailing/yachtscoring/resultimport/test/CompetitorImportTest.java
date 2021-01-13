package com.sap.sailing.yachtscoring.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.resultimport.impl.ResultUrlRegistryImpl;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.yachtscoring.resultimport.AbstractYachtScoringProvider;
import com.sap.sailing.yachtscoring.resultimport.YachtScoringCompetitorProvider;
import com.sap.sse.common.Util;

@SuppressWarnings("restriction")
public class CompetitorImportTest extends AbstractCharlstonRaceWeek2015Test {
    private static final String URL_EVENT= "J Fest 2020, St. Petersburg, FL, USA";
    
    @Test
    public void simpleCompetitorImportTest() throws FileNotFoundException, IOException, JAXBException {
        ResultUrlRegistry resultUrlRegistry = mock(ResultUrlRegistry.class);
        when(resultUrlRegistry.getReadableResultUrls(AbstractYachtScoringProvider.NAME)).thenReturn(Arrays.asList(getClass().getClassLoader().getResource(CHARLSTONRACEWEEK2015_TESTFILE_XRR)));
        final YachtScoringCompetitorProvider competitorProvider = new YachtScoringCompetitorProvider(ParserFactory.INSTANCE, resultUrlRegistry, getTestDocumentProvider());
        assertTrue(competitorProvider.getHasCompetitorsForRegattasInEvent().containsKey(CHARLSTONRACEWEEK2015_EVENT_NAME));
        assertTrue(competitorProvider.getHasCompetitorsForRegattasInEvent().get(CHARLSTONRACEWEEK2015_EVENT_NAME).contains(BOAT_CLASS_J111));
        final Iterable<CompetitorDescriptor> competitorDescriptors = competitorProvider.getCompetitorDescriptors(CHARLSTONRACEWEEK2015_EVENT_NAME, null); // get competitors for all regattas in event
        assertNotNull(competitorDescriptors);
        assertEquals(4449, Util.size(competitorDescriptors));
        final Iterable<CompetitorDescriptor> competitorDescriptorsJ111 = competitorProvider.getCompetitorDescriptors(CHARLSTONRACEWEEK2015_EVENT_NAME, BOAT_CLASS_J111); // get competitors only for J111 regatta
        assertNotNull(competitorDescriptorsJ111);
        assertTrue(Util.size(competitorDescriptors) > Util.size(competitorDescriptorsJ111));
        assertEquals(353, Util.size(competitorDescriptorsJ111));
    }
    
    @Test
    public void simpleCompetitorImportConnectionTest() throws FileNotFoundException, IOException, JAXBException {
        final RacingEventService racingEventService = new RacingEventServiceImpl();
        final DomainObjectFactory domainObjectFactory = racingEventService.getDomainObjectFactory();
        final MongoObjectFactory mongoObjectFactory = racingEventService.getMongoObjectFactory();
        ResultUrlRegistry resultUrlRegistry = new ResultUrlRegistryImpl(mongoObjectFactory, domainObjectFactory);
        URL url = new URL("https://www.yachtscoring.com/results_xrr_auto.cfm?eid=9649");
        resultUrlRegistry.registerResultUrl(AbstractYachtScoringProvider.NAME, url);
        final YachtScoringCompetitorProvider competitorProvider = new YachtScoringCompetitorProvider(ParserFactory.INSTANCE, resultUrlRegistry);
        assertTrue(competitorProvider.getHasCompetitorsForRegattasInEvent().containsKey(URL_EVENT));
        assertTrue(competitorProvider.getHasCompetitorsForRegattasInEvent().get(URL_EVENT).contains(BOAT_CLASS_J111));
        final Iterable<CompetitorDescriptor> competitorDescriptors = competitorProvider.getCompetitorDescriptors(URL_EVENT, null); // get competitors for all regattas in event
        assertNotNull(competitorDescriptors);
//        assertEquals(62, Util.size(competitorDescriptors));
        final Iterable<CompetitorDescriptor> competitorDescriptorsJ111 = competitorProvider.getCompetitorDescriptors(URL_EVENT, BOAT_CLASS_J111); // get competitors only for J111 regatta
        assertNotNull(competitorDescriptorsJ111);
        assertTrue(Util.size(competitorDescriptors) >Util.size(competitorDescriptorsJ111));
//        assertEquals(134, Util.size(competitorDescriptorsJ111));
    }
}
