package com.sap.sailing.manage2sail.resultimport;

import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sailing.domain.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.AbstractResultUrlProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.xrr.resultimport.ParserFactory;

public abstract class AbstractManage2SailProvider extends AbstractResultUrlProvider implements ResultUrlProvider {
    private static final long serialVersionUID = 2275835750471711783L;

    public static final String NAME = "Manage2Sail XRR Result Importer";

    protected static final String EVENT_ID_REGEX = "^[\\da-f]{8}(-[\\da-f]{4}){3}-[\\da-f]{12}$";
    protected static final String EVENT_ID_TEMPLATE = "http://manage2sail.com/api/public/links/event/%s?accesstoken=bDAv8CwsTM94ujZ&mediaType=json";

    private final ParserFactory parserFactory;

    protected AbstractManage2SailProvider(ParserFactory parserFactory, ResultUrlRegistry resultUrlRegistry) {
        super(resultUrlRegistry);
        this.parserFactory = parserFactory;
    }

    protected ParserFactory getParserFactory() {
        return parserFactory;
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
        return "http://manage2sail.com/api/public/links/event/d30883d3-2876-4d7e-af49-891af6cbae1b?accesstoken=bDAv8CwsTM94ujZ&mediaType=json";
    }
}
