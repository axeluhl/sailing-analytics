package com.sap.sailing.domain.yellowbrickadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import com.sap.sailing.competitorimport.CompetitorProvider;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.PersonDTO;
import com.sap.sailing.resultimport.AbstractResultUrlProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sse.common.CountryCodeFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;
import com.sap.sse.util.CSVParser;
import com.sap.sse.util.HttpUrlConnectionHelper;

public class YellowBrickCompetitorProvider extends AbstractResultUrlProvider implements CompetitorProvider {
    private static final long serialVersionUID = -2577277045205748666L;
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";
    private static final String BOAT_ID_PREFIX = "YB-";
    private final ResourceBundleStringMessagesImpl stringMessages;
    private final String RACE_NAME_HEADER = "Race Name";
    private final String SAIL_NUMBER_HEADER = "Sail Number";
    private final String MODEL_HEADER = "Model";
    private final String ENTRANT_HEADER = "Entrant";
    private final String COUNTRY_HEADER = "Country";
    private final String SKIPPER_HEADER = "Skipper";
    private final String IRC_TOT_HEADER = "IRC";
    private final String YELLOWBRICK_NAME_HEADER = "YellowBrick Name";
    private final String COMPETITOR_ID_HEADER = "Competitor ID";
    
    public YellowBrickCompetitorProvider(ResultUrlRegistry resultUrlRegistry) {
        super(resultUrlRegistry);
        stringMessages = new ResourceBundleStringMessagesImpl(STRING_MESSAGES_BASE_NAME, getClass().getClassLoader());
    }

    @Override
    public String getName() {
        return "YellowBrick Competitor Provider";
    }

    @Override
    public Map<String, Set<String>> getHasCompetitorsForRegattasInEvent() throws IOException {
        final Map<String, Set<String>> result = new HashMap<>();
        for (final URL url : getReadableUrls()) {
            result.put(url.toString(), null);
        }
        return result;
    }

    @Override
    public Iterable<CompetitorDescriptor> getCompetitorDescriptors(String eventNameWhichIsTheURL, String regattaNameWhichIsIgnored)
            throws JAXBException, IOException {
        // assumes that the eventName is the URL
        final Set<CompetitorDescriptor> result = new HashSet<>();
        final InputStream inputStream = (InputStream) HttpUrlConnectionHelper.redirectConnection(new URL(eventNameWhichIsTheURL)).getContent();
        final CSVParser csvParser = new CSVParser();
        final Pair<List<String>, Iterable<List<String>>> content = csvParser.parseWithHeader(new InputStreamReader(inputStream));
        for (final List<String> competitorLine : content.getB()) {
            result.add(parseIntoCompetitorDescriptor(/* header */ content.getA(), competitorLine));
        }
        return result;
    }

    private CompetitorDescriptor parseIntoCompetitorDescriptor(List<String> header, List<String> competitorLine) {
        final String competitorIdString = getStringFromCsv(COMPETITOR_ID_HEADER, header, competitorLine);
        final String competitorId = competitorIdString == null ? UUID.randomUUID().toString() : competitorIdString;
        final String fullName = getStringFromCsv(SKIPPER_HEADER, header, competitorLine);
        final String shortName = getStringFromCsv(ENTRANT_HEADER, header, competitorLine);
        final String teamName = getStringFromCsv(RACE_NAME_HEADER, header, competitorLine);
        final String countryCode = getStringFromCsv(COUNTRY_HEADER, header, competitorLine);
        final String ircToTString = getStringFromCsv(IRC_TOT_HEADER, header, competitorLine);
        final Double timeOnTimeFactor = Util.hasLength(ircToTString) ? Double.valueOf(ircToTString) : null;
        final String boatId = BOAT_ID_PREFIX+getStringFromCsv(YELLOWBRICK_NAME_HEADER, header, competitorLine);
        final String boatName = getStringFromCsv(RACE_NAME_HEADER, header, competitorLine);
        final String boatClassName = getStringFromCsv(MODEL_HEADER, header, competitorLine);
        final String sailNumber = getStringFromCsv(SAIL_NUMBER_HEADER, header, competitorLine);
        final Iterable<PersonDTO> persons = Collections.singleton(new PersonDTO(fullName, /* dateOfBirth */ null, /* description */ null, countryCode));
        return new CompetitorDescriptor(/* event */ "", /* regatta */ "", /* race */ "", /* fleet */ "", competitorId,
                fullName, shortName, teamName, persons,
                CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName(countryCode), timeOnTimeFactor,
                /* timeOnDistanceAllowancePerNauticalMile */ null, boatId, boatName, boatClassName, sailNumber);
    }
    
    private String getStringFromCsv(String field, List<String> headers, List<String> parsedLine) {
        final int index = headers.indexOf(field);
        final String result;
        if (index >= parsedLine.size()) {
            result = null;
        } else {
            result = parsedLine.get(index);
        }
        return result;
    }

    @Override
    public String getHint(Locale locale) {
        return stringMessages.get(locale, "CompetitorImportHint");
    }

    @Override
    public URL resolveUrl(String url) throws MalformedURLException {
        return new URL(url);
    }

    @Override
    public String getOptionalSampleURL() {
        return null;
    }
}
