package com.sap.sailing.manage2sail.resultimport;

import java.net.URL;

import com.sap.sailing.domain.resultimport.ResultUrlProvider;
import com.sap.sailing.manage2sail.RegattaResultDescriptor;

public class CompetitorDocumentProvider extends AbstractManage2SailResultDocumentProvider {
    public CompetitorDocumentProvider(ResultUrlProvider resultUrlProvider) {
        super(resultUrlProvider);
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
