package com.sap.sailing.manage2sail.resultimport;

import java.io.IOException;
import java.net.URL;

import com.sap.sailing.manage2sail.RegattaResultDescriptor;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultUrlProvider;

public class CompetitorDocumentProvider extends AbstractManage2SailResultDocumentProvider {
    public CompetitorDocumentProvider(ResultUrlProvider resultUrlProvider) {
        super(resultUrlProvider);
    }

    @Override
    public Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException {
        return super.getResultDocumentDescriptors();
    }

    @Override
    protected URL getDocumentUrlForRegatta(RegattaResultDescriptor regattaResult) {
        return regattaResult.getXrrEntriesUrl();
    }

    @Override
    protected boolean acceptRegatta(RegattaResultDescriptor regattaResult) {
        return true; // no further requirements; doesn't need to be final nor have an appropriate time point
    }
}
