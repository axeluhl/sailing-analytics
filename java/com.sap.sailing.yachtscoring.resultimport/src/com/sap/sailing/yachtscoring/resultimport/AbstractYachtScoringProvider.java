package com.sap.sailing.yachtscoring.resultimport;

import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sailing.domain.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.AbstractResultUrlProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.xrr.resultimport.ParserFactory;

public abstract class AbstractYachtScoringProvider extends AbstractResultUrlProvider implements ResultUrlProvider {
    private static final long serialVersionUID = 629585353618956893L;
    
    public static final String NAME = "YachtScoring XRR Result Importer";
    public static final String EVENT_ID_REGEX = "^\\d+$";
    public static final String EVENT_ID_TEMPLATE = "https://www.yachtscoring.com/results_xrr_auto.cfm?eid=%s";
    protected final ParserFactory parserFactory;
    
    protected AbstractYachtScoringProvider(ParserFactory parserFactory, ResultUrlRegistry resultUrlRegistry) {
        super(resultUrlRegistry);
        this.parserFactory = parserFactory;
    }
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public URL resolveUrl(String url) throws MalformedURLException {
        String completedUrl = url;
        if (url.matches(EVENT_ID_REGEX)) {
            completedUrl = String.format(EVENT_ID_TEMPLATE, url);
        }
        return new URL(completedUrl); //TODO Find a better way to check if a URL is valid
    }

    @Override
    public String getOptionalSampleURL() {
        return String.format(EVENT_ID_TEMPLATE, /* event ID */ "1220");
    }

}