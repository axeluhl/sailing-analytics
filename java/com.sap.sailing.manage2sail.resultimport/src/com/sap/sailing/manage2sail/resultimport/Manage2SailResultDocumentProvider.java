package com.sap.sailing.manage2sail.resultimport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;

public class Manage2SailResultDocumentProvider implements ResultDocumentProvider {
    private final ResultUrlProvider resultUrlProvider;

    public Manage2SailResultDocumentProvider(final ResultUrlProvider resultUrlProvider) {
        this.resultUrlProvider = resultUrlProvider;
    }

    @Override
    public Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException {
        List<ResultDocumentDescriptor> result = new ArrayList<>();
        Manage2SailEventResultsParser parser = new Manage2SailEventResultsParser();
        for (URL url : resultUrlProvider.getUrls()) {
            URLConnection eventResultConn = url.openConnection();
            EventResultDescriptor eventResult = parser.getEventResult((InputStream) eventResultConn.getContent());
            if (eventResult != null) {
                for (RegattaResultDescriptor regattaResult : eventResult.getRegattaResults()) {
                    if (regattaResult.getIsFinal() != null) {
                        if (regattaResult.getIsFinal() && regattaResult.getXrrFinalUrl() != null) {
                            URLConnection regattaResultConn = regattaResult.getXrrFinalUrl().openConnection();
                            TimePoint lastModified = regattaResult.getLastModified() != null ? regattaResult
                                    .getLastModified() : new MillisecondsTimePoint(regattaResultConn.getLastModified());
                            result.add(new ResultDocumentDescriptorImpl((InputStream) regattaResultConn.getContent(),
                                    regattaResult.getXrrPreliminaryUrl().toString(), lastModified, eventResult
                                            .getName(), regattaResult.getName(), regattaResult.getBoatClass()));
                        } else if (regattaResult.getXrrPreliminaryUrl() != null) {
                            URLConnection regattaResultConn = regattaResult.getXrrPreliminaryUrl().openConnection();
                            TimePoint lastModified = regattaResult.getLastModified() != null ? regattaResult
                                    .getLastModified() : new MillisecondsTimePoint(regattaResultConn.getLastModified());
                            result.add(new ResultDocumentDescriptorImpl((InputStream) regattaResultConn.getContent(),
                                    regattaResult.getXrrPreliminaryUrl().toString(), lastModified, eventResult
                                            .getName(), regattaResult.getName(), regattaResult.getBoatClass()));
                        }
                    }
                }
            }
        }
        return result;
    }
}
