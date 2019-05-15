package com.sap.sailing.manage2sail.resultimport;

import java.net.URL;

import com.sap.sailing.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.xrr.resultimport.ParserFactory;

public abstract class AbstractManage2SailProvider implements ResultUrlProvider {
    private static final long serialVersionUID = 2275835750471711783L;

    public static final String NAME = "Manage2Sail XRR Result Importer";

    private final ParserFactory parserFactory;
    private final ResultUrlRegistry resultUrlRegistry;

    protected AbstractManage2SailProvider(ParserFactory parserFactory, ResultUrlRegistry resultUrlRegistry) {
        super();
        this.parserFactory = parserFactory;
        this.resultUrlRegistry = resultUrlRegistry;
    }

    protected ParserFactory getParserFactory() {
        return parserFactory;
    }

    protected ResultUrlRegistry getResultUrlRegistry() {
        return resultUrlRegistry;
    }

    @Override
    public Iterable<URL> getUrls() {
        return getResultUrlRegistry().getResultUrls(NAME);
    }

    @Override
    public String getOptionalSampleURL() {
        return "http://manage2sail.com/api/public/links/event/d30883d3-2876-4d7e-af49-891af6cbae1b?accesstoken=bDAv8CwsTM94ujZ&mediaType=json";
    }
}
