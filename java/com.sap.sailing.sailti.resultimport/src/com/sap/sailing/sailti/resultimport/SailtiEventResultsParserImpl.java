package com.sap.sailing.sailti.resultimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.schema.Event;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.HttpUrlConnectionHelper;

/**
 * From a Sailti event result overview document that consists of pairs of class names and the names / URLs of the XRR documents
 * for that class can parse the document into an {@link EventResultDescriptor} from which the regatta results can be obtained
 * using {@link EventResultDescriptor#getRegattaResults()}.<p>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SailtiEventResultsParserImpl implements SailtiEventResultsParser {
    private static final Logger logger = Logger.getLogger(SailtiEventResultsParserImpl.class.getName());
    
    static final Pattern eventNamePattern = Pattern.compile("<h2>([^<]*)</h2>");
    static final Pattern xrrFileNamePattern = Pattern.compile("XML-([^_]*)_([0-9][0-9]*)_([0-9][0-9]*)_([0-9]*).xml");
    static final Pattern classAndXrrLinkPattern = Pattern.compile("<p>([^<]*)<br/>\\s*<a *href=\"(([^\"]*)"+xrrFileNamePattern+")\"\\s*>");

    private final URL baseUrl;
    
    public SailtiEventResultsParserImpl(URL baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    /**
     * @param is closed before the method returns, also in case of exception
     */
    public EventResultDescriptor getEventResult(InputStream is) throws IOException {
        EventResultDescriptor result = null;
        try {
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                List<RegattaResultDescriptor> regattaResults = new ArrayList<>();
                final StringBuilder eventHtml = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    eventHtml.append(line);
                }
                final String eventHtmlAsString = eventHtml.toString();
                final Matcher matcher = classAndXrrLinkPattern.matcher(eventHtmlAsString);
                while (matcher.find()) {
                    regattaResults.add(new RegattaResultDescriptor(matcher.group(1)+"/"+matcher.group(5)+"/"+matcher.group(6), matcher.group(1), getBoatClassName(matcher),
                            new URL(baseUrl, getAbsoluteXrrUrlPath(matcher)), getTimePoint(matcher)));
                }
                final String eventName;
                final Matcher eventNameMatcher = eventNamePattern.matcher(eventHtmlAsString);
                if (eventNameMatcher.find()) {
                    eventName = eventNameMatcher.group(1);
                } else if (regattaResults.isEmpty()) {
                    eventName = getEventId();
                } else {
                    final URL xrrFinalUrl = regattaResults.iterator().next().getXrrFinalUrl();
                    RegattaResults anyXrr = null;
                    try {
                        anyXrr = ParserFactory.INSTANCE.createParser(
                                HttpUrlConnectionHelper.redirectConnection(xrrFinalUrl).getInputStream(), xrrFinalUrl.toString()).parse();
                    } catch (Exception e) {
                        logger.warning("Couldn't read XRR document from "+xrrFinalUrl+" to obtain event name; using event ID "+getEventId()+" instead");
                    }
                    if (anyXrr == null) {
                        eventName = getEventId();
                    } else {
                        eventName = anyXrr.getPersonOrBoatOrTeam().stream().filter(o->(o instanceof Event)).findAny().map(o->((Event) o).getTitle()).get();
                    }
                }
                result = new EventResultDescriptor(getEventId(), eventName, regattaResults);
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Problem parsing Sailti event document", e);
        }
        return result;
    }
    
    private String getEventId() {
        return baseUrl.getPath().substring(baseUrl.getPath().lastIndexOf('/')+1);
    }
    
    private String getAbsoluteXrrUrlPath(Matcher matcher) {
        return matcher.group(2);
    }

    String getBoatClassName(Matcher matcher) {
        return matcher.group(1);
    }
    
    TimePoint getTimePoint(Matcher matcher) throws ParseException {
        return TimePoint.of(new SimpleDateFormat("yyyyMMddhhmmssX").parse(matcher.group(7)+"Z"));
    }
}
