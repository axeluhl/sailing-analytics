package com.sap.sailing.sailti.resultimport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;
import com.sap.sailing.xrr.resultimport.impl.XRRParserUtil;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.Event;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.HttpUrlConnectionHelper;

/**
 * A sailti.com specific result document provider reading the complete XRR document of an event to find all regattas
 * inside.
 * 
 * @author Frank
 * @author Axel Uhl (d043530)
 *
 */
public class SailtiResultDocumentProvider implements ResultDocumentProvider {
    private final ResultUrlProvider resultUrlProvider;

    public SailtiResultDocumentProvider(ResultUrlProvider resultUrlProvider) {
        this.resultUrlProvider = resultUrlProvider;
    }

    public List<ResultDocumentDescriptor> resolveResultDocumentDescriptors(RegattaResults xrrParserResult, URL url) {
        List<ResultDocumentDescriptor> result = new ArrayList<>();
        final TimePoint xrrDocumentDateAndTime = XRRParserUtil.calculateTimePointForRegattaResults(xrrParserResult);
        for (Object o : xrrParserResult.getPersonOrBoatOrTeam()) {
            if (o instanceof Event) {
                Event event = (Event) o;
                String eventName = event.getTitle();
                for (Object d: event.getRaceOrDivisionOrRegattaSeriesResult()) {
                    if (d instanceof Division) {
                        Division division = (Division) d;
                        String regattaName = division.getTitle();
                        String boatClass = division.getTitle();
                        try {
                            String requestUrl = url.toString() + "&Class=" + URLEncoder.encode(boatClass, "UTF-8");
                            URL urlByClass = new URL(requestUrl);
                            result.add(new UrlResultDocumentDescriptorImpl(urlByClass, requestUrl, xrrDocumentDateAndTime,
                                    eventName, regattaName, boatClass));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return result;
    }
    

    private URL getDocumentUrlForRegatta(RegattaResultDescriptor regattaResult) {
        return regattaResult.getXrrFinalUrl();
    }

    private boolean acceptRegatta(RegattaResultDescriptor regattaResult) {
        return regattaResult.getPublishedAt() != null;
    }

    @Override
    public Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException {
        List<ResultDocumentDescriptor> result = new ArrayList<>();
        SailtiEventResultsParserImpl parser = new SailtiEventResultsParserImpl();
        for (URL url : resultUrlProvider.getReadableUrls()) {
            URLConnection eventResultConn = HttpUrlConnectionHelper.redirectConnection(url);
            EventResultDescriptor eventResult = parser.getEventResult((InputStream) eventResultConn.getContent());
            addResultsForEvent(result, eventResult);
        }
        return result;
    }

    private void addResultsForEvent(List<ResultDocumentDescriptor> result, EventResultDescriptor eventResult)
            throws IOException {
        if (eventResult != null) {
            for (RegattaResultDescriptor regattaResult : eventResult.getRegattaResults()) {
                if (acceptRegatta(regattaResult)) {
                    final String boatClass = regattaResult.getClassName(); 
                    final URL resultUrl = getDocumentUrlForRegatta(regattaResult);
                    if (resultUrl != null) {
                        final URLConnection regattaResultConn = HttpUrlConnectionHelper.redirectConnection(resultUrl);
                        result.add(new ResultDocumentDescriptorImpl((InputStream) regattaResultConn.getContent(),
                                resultUrl.toString(), regattaResult.getPublishedAt(),
                                eventResult.getName(), regattaResult.getName(), boatClass));
                    }
                }
            }
        }
    }
}
