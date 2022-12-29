package com.sap.sailing.domain.yellowbrickadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
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
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;
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
    private final ResourceBundleStringMessagesImpl stringMessages;
    private final String RACE_NAME_HEADER = "Race Name";
    private final String SAIL_NUMBER_HEADER = "Sail Number";
    private final String MODEL_HEADER = "Model";
    private final String ENTRANT_HEADER = "Entrant";
    private final String COUNTRY_HEADER = "Country";
    private final String SKIPPER_HEADER = "Skipper";
    private final String IRC_TOT_HEADER = "IRC";
    private final String YELLOWBRICK_NAME_HEADER = "YellowBrick Name";
    /**
     * The values in the "Competitor ID" column are expected to be the "YB" prefix, plus the race URL, separated by
     * a leading and a trailing dash, followed by the YB Boat Name. Example: "YB-rmsr2021-ATAME" 
     */
    private final String COMPETITOR_ID_HEADER = "Competitor ID";
    private final String CATEGORY_IRC_HEADER = "cat-IRC";
    // The following two header fields also exist but are not currently used for anything here:
//    private final String MOCRA_HEADER = "MOCRA";
//    private final String DH_HEADER = "DH";
    private final String ORC_HEADER = "ORC only";
    
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
        final URLConnection connection = HttpUrlConnectionHelper.redirectConnection(new URL(eventNameWhichIsTheURL));
        final Charset charset = HttpUrlConnectionHelper.getCharsetFromConnectionOrDefault(connection, "UTF-8");
        final InputStream inputStream = (InputStream) connection.getContent();
        final CSVParser csvParser = new CSVParser();
        final Pair<List<String>, Iterable<List<String>>> content = csvParser.parseWithHeader(new InputStreamReader(inputStream, charset));
        for (final List<String> competitorLine : content.getB()) {
            result.add(parseIntoCompetitorDescriptor(/* header */ content.getA(), competitorLine));
        }
        return result;
    }

    private CompetitorDescriptor parseIntoCompetitorDescriptor(List<String> header, List<String> competitorLine) {
        final String fullName = getStringFromCsv(SKIPPER_HEADER, header, competitorLine);
        final String shortName = getStringFromCsv(ENTRANT_HEADER, header, competitorLine);
        final String teamName = getStringFromCsv(RACE_NAME_HEADER, header, competitorLine);
        final String countryCode = getStringFromCsv(COUNTRY_HEADER, header, competitorLine);
        final String ircToTString = getStringFromCsv(IRC_TOT_HEADER, header, competitorLine);
        final Double timeOnTimeFactor = Util.hasLength(ircToTString) ? Double.valueOf(ircToTString) : 1.0;
        final String ybName = getStringFromCsv(YELLOWBRICK_NAME_HEADER, header, competitorLine);
        final String boatId = YellowBrickTrackingAdapter.getBoatId(ybName);
        final String competitorIdString = getStringFromCsv(COMPETITOR_ID_HEADER, header, competitorLine);
        final String competitorId = competitorIdString == null ? UUID.randomUUID().toString() : competitorIdString;
        final String boatName = getStringFromCsv(RACE_NAME_HEADER, header, competitorLine);
        final String boatClassName = getStringFromCsv(MODEL_HEADER, header, competitorLine);
        final String sailNumber = getStringFromCsv(SAIL_NUMBER_HEADER, header, competitorLine);
        final Iterable<PersonDTO> persons = Collections.singleton(new PersonDTO(fullName, /* dateOfBirth */ null, /* description */ null, countryCode));
        final String ircCategory = getStringFromCsv(CATEGORY_IRC_HEADER, header, competitorLine);
        final String orcOnly = getStringFromCsv(ORC_HEADER, header, competitorLine);
        final String race;
        final String fleet;
        if (Util.hasLength(orcOnly)) {
            if (ircCategory.equals("undefined")) {
                race = "ORC";
                fleet = "ORC";
            } else {
                race = "ORC/IRC";
                fleet = ircCategory;
            }
        } else {
            race = "IRC";
            fleet = ircCategory;
        }
        return new CompetitorDescriptor(/* event */ "", /* regatta */ "", race, fleet, competitorId,
                Util.hasLength(fullName)?fullName:boatName, shortName, teamName, persons,
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
