package com.sap.sailing.manage2sail.resultimport;

import java.net.URL;

import com.sap.sailing.manage2sail.RegattaResultDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.ResultUrlProvider;

public class Manage2SailResultDocumentProvider extends AbstractManage2SailResultDocumentProvider implements ResultDocumentProvider {

    public Manage2SailResultDocumentProvider(final ResultUrlProvider resultUrlProvider) {
        super(resultUrlProvider);
    }

    @Override
    protected URL getDocumentUrlForRegatta(RegattaResultDescriptor regattaResult) {
        final URL resultUrl;
        if (regattaResult.getIsFinal() && regattaResult.getXrrFinalUrl() != null) {
            resultUrl = regattaResult.getXrrFinalUrl();
        } else if (regattaResult.getXrrPreliminaryUrl() != null) {
            resultUrl = regattaResult.getXrrPreliminaryUrl();
        } else {
            resultUrl = null;
        }
        return resultUrl;
    }

    @Override
    protected boolean acceptRegatta(RegattaResultDescriptor regattaResult) {
        return regattaResult.getIsFinal() != null && regattaResult.getPublishedAt() != null;
    }

}
