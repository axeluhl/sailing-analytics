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
    private static String EVENT_ID_TEMPLATE;

    private final ParserFactory parserFactory;

    protected AbstractManage2SailProvider(ParserFactory parserFactory, ResultUrlRegistry resultUrlRegistry) {
        super(resultUrlRegistry);
        this.parserFactory = parserFactory;
        EVENT_ID_TEMPLATE = "https://"+com.sap.sailing.manage2sail.Activator.getInstance().getManage2SailHostname()+"/api/public/links/event/%s?mediaType=json";
    }

    protected ParserFactory getParserFactory() {
        return parserFactory;
    }

    @Override
    public URL resolveUrl(String url) throws MalformedURLException {
        String completedUrl = url;
        if (url.matches(EVENT_ID_REGEX)) {
            completedUrl = String.format(getEventIdTemplate(), url);
        }
        return new URL(completedUrl); // TODO Find a better way to check if a URL is valid
    }

    @Override
    public String getOptionalSampleURL() {
        return String.format(getEventIdTemplate(), "d30883d3-2876-4d7e-af49-891af6cbae1b");
    }

    protected static String getEventIdTemplate() {
        return EVENT_ID_TEMPLATE;
    }
}
