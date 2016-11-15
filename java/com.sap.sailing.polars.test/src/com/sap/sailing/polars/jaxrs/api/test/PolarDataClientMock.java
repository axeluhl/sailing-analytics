package com.sap.sailing.polars.jaxrs.api.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.polars.jaxrs.client.PolarDataClient;

public class PolarDataClientMock extends PolarDataClient {

    private final File file;

    public PolarDataClientMock(File file, PolarDataServiceImpl polarDataServiceImpl,
            SharedDomainFactory domainFactory) {
        super(null, polarDataServiceImpl, domainFactory);
        this.file = file;
    }

    @Override
    protected JSONObject getJsonFromResponse() throws IOException, ParseException {
        return (JSONObject) new JSONParser().parse(new FileReader(file));
    }

}
