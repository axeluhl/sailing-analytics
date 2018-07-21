package com.sap.sailing.manage2sail.resultimport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.manage2sail.EventResultDescriptor;
import com.sap.sailing.manage2sail.Manage2SailEventResultsParserImpl;
import com.sap.sailing.manage2sail.RegattaResultDescriptor;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.HttpUrlConnectionHelper;

public abstract class AbstractManage2SailResultDocumentProvider {
    private final ResultUrlProvider resultUrlProvider;

    public AbstractManage2SailResultDocumentProvider(final ResultUrlProvider resultUrlProvider) {
        this.resultUrlProvider = resultUrlProvider;
    }

    public Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException {
        List<ResultDocumentDescriptor> result = new ArrayList<>();
        Manage2SailEventResultsParserImpl parser = new Manage2SailEventResultsParserImpl();
        for (URL url : resultUrlProvider.getUrls()) {
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
                // Depending on the regatta type the boat class in the XRR is set or not (in the division tag)
                // Olympic boat classes -> IFClassID="2.4M" Title="2.4M open"
                // International boat classes -> IFClassID="" Title="Coachboat"
                // Open boat classes (ORC) ->  IFClassID="" Title="Welcome Race ORC-Club"
                // -> therefore we need to take the regatta title as boat class where the IsafID is not available
                String boatClass = regattaResult.getIsafId() != null && !regattaResult.getIsafId().isEmpty() ? regattaResult.getIsafId() : regattaResult.getName();
                if (acceptRegatta(regattaResult)) {
                    final URL resultUrl = getDocumentUrlForRegatta(regattaResult);
                    if (resultUrl != null) {
                        URLConnection regattaResultConn = HttpUrlConnectionHelper.redirectConnection(resultUrl);
                        result.add(new ResultDocumentDescriptorImpl((InputStream) regattaResultConn.getContent(),
                                resultUrl.toString(), regattaResult.getPublishedAt()==null?null:new MillisecondsTimePoint(regattaResult.getPublishedAt()),
                                eventResult.getName(), regattaResult.getName(), boatClass, regattaResult
                                        .getCompetitorGenderType()));
                    }
                }
            }
        }
    }

    abstract protected boolean acceptRegatta(RegattaResultDescriptor regattaResult);
    
    abstract protected URL getDocumentUrlForRegatta(RegattaResultDescriptor regattaResult);
}
