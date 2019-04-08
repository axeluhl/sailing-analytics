package com.sap.sailing.yachtscoring.resultimport;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultUrlProvider;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.impl.UrlBasedXRRResultDocumentProvider;
import com.sap.sailing.xrr.resultimport.impl.XRRParserUtil;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.Event;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sse.common.TimePoint;

/**
 * A yachtscoring.com specific result document provider reading the complete XRR document of an event to find all regattas inside.
 * @author Frank
 *
 */
public class YachtscoringResultDocumentProvider extends UrlBasedXRRResultDocumentProvider {

    public YachtscoringResultDocumentProvider(ResultUrlProvider resultUrlProvider, ParserFactory parserFactory) {
        super(resultUrlProvider, parserFactory);
    }

    public List<ResultDocumentDescriptor> resolveResultDocumentDescriptors(RegattaResults xrrParserResult, URL url) {
        List<ResultDocumentDescriptor> result = new ArrayList<>();
        TimePoint xrrDocumentDateAndTime = XRRParserUtil.calculateTimePointForRegattaResults(xrrParserResult);
        
        for (Object o : xrrParserResult.getPersonOrBoatOrTeam()) {
            if (o instanceof Event) {
                Event event = (Event) o;
                String eventName = event.getTitle();
                for(Object d: event.getRaceOrDivisionOrRegattaSeriesResult()) {
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
}
