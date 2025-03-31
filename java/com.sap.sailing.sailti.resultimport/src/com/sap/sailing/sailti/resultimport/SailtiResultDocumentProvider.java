package com.sap.sailing.sailti.resultimport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;
import com.sap.sse.util.HttpUrlConnectionHelper;

/**
 * A sailti.com specific result document provider reading the complete XRR document of an event to find all regattas
 * inside.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SailtiResultDocumentProvider implements ResultDocumentProvider {
    private final ResultUrlProvider resultUrlProvider;

    public SailtiResultDocumentProvider(ResultUrlProvider resultUrlProvider) {
        this.resultUrlProvider = resultUrlProvider;
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
        for (URL url : resultUrlProvider.getReadableUrls()) {
            SailtiEventResultsParserImpl parser = new SailtiEventResultsParserImpl(url);
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
