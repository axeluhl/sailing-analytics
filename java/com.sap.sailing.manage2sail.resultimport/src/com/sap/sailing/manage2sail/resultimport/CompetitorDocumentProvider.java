package com.sap.sailing.manage2sail.resultimport;

import java.net.URL;

import com.sap.sailing.manage2sail.RegattaResultDescriptor;
import com.sap.sailing.resultimport.ResultUrlProvider;

public class CompetitorDocumentProvider extends AbstractManage2SailResultDocumentProvider {
    public CompetitorDocumentProvider(ResultUrlProvider resultUrlProvider) {
        super(resultUrlProvider);
    }

    @Override
    protected URL getDocumentUrlForRegatta(RegattaResultDescriptor regattaResult) {
        return regattaResult.getXrrEntriesUrl();
    }
}
