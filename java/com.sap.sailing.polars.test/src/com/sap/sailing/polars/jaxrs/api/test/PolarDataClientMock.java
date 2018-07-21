package com.sap.sailing.polars.jaxrs.api.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.polars.ReplicablePolarService;
import com.sap.sailing.polars.jaxrs.client.PolarDataClient;

public class PolarDataClientMock extends PolarDataClient {

    private final File file;

    public PolarDataClientMock(File file, ReplicablePolarService polarService, DomainFactory domainFactory) {
        super(null, polarService);
        polarService.registerDomainFactory(domainFactory);
        this.file = file;
    }

    @Override
    protected InputStream getContentFromResponse() throws IOException, ParseException {
        return new FileInputStream(file);
    }

}
