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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sse.common.TimePoint;

/**
 * From a Sailti event result overview document that consists of pairs of class names and the names / URLs of the XRR documents
 * for that class can parse the document into an {@link EventResultDescriptor} from which the regatta results can be obtained
 * using {@link EventResultDescriptor#getRegattaResults()}.<p>
 * 
 * TODO bug5693 Sailti URL format...: URLs should be of the form
 * <pre>
 * http://manage2sail.com/api/public/links/event/d30883d3-2876-4d7e-af49-891af6cbae1b?accesstoken=bDAv8CwsTM94ujZ&mediaType=json
 * </pre>
 * where the UUID following the <code>event</code> path element represents the event ID. Events can be
 * discovered by the manage2sail.com website.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SailtiEventResultsParserImpl implements SailtiEventResultsParser {
    private static final Logger logger = Logger.getLogger(SailtiEventResultsParserImpl.class.getName());

    static final Pattern xrrFileNamePattern = Pattern.compile("XML-(.*)_([0-9][0-9]*)_([0-9][0-9]*)_([0-9]*).xml");
    
    /**
     * @param is closed before the method returns, also in case of exception
     */
    public EventResultDescriptor getEventResult(InputStream is) throws IOException {
        EventResultDescriptor result = null;
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            List<RegattaResultDescriptor> regattaResults = new ArrayList<>();
            String className;
            while ((className = br.readLine()) != null) {
                final String xrrDocumentUrl = br.readLine();
                final Matcher matcher = xrrFileNamePattern.matcher(xrrDocumentUrl);
                if (matcher.matches()) {
                    regattaResults.add(new RegattaResultDescriptor(matcher.group(2), matcher.group(3), className, new URL(xrrDocumentUrl), getTimePoint(matcher)));
                }
            }
            result = new EventResultDescriptor(/* TODO bug5693 ID */ ""+new Random().nextDouble(),
                    /* TODO bug5693 name */  ""+new Random().nextDouble(),
                    regattaResults);
            is.close();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Problem parsing Sailti event document", e);
        } finally { 
            is.close();
        }
        return result;
    }
    
    String getBoatClassName(Matcher matcher) {
        return matcher.group(1);
    }
    
    TimePoint getTimePoint(Matcher matcher) throws ParseException {
        return TimePoint.of(new SimpleDateFormat("yyyyMMddhhmmssX").parse(matcher.group(4)+"Z"));
    }
}
