package com.sap.sailing.windestimation.data.importer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.json.simple.parser.ParseException;

import com.sap.sailing.windestimation.util.LoggingUtil;

public class PolarDataImporter {

    private static final String polarDataSourceUrl = "https://www.sapsailing.com";

    public static final String polarDataFilePath = "polarData.dat";

    private static final String RESOURCE = "polars/api/polar_data";

    public PolarDataImporter() {
    }

    public InputStream retrievePolarDataRegressionAsBytes() throws IOException, ParseException {
        LoggingUtil.logInfo("Loading polar regression data from remote server " + polarDataSourceUrl);
        final InputStream inputStream = getContentFromResponse();
        LoggingUtil.logInfo("Loading polar regression data succeeded");
        return inputStream;
    }

    public void persistPolarDataRegressionAsBytes(File targetFile, InputStream inputStream) throws IOException {
        LoggingUtil.logInfo("Persisting polar regression data");
        FileUtils.copyInputStreamToFile(inputStream, targetFile);
        LoggingUtil.logInfo("Persisting polar regression data succeeded");
    }

    private String getAPIString() {
        return polarDataSourceUrl + (polarDataSourceUrl.endsWith("/") ? "" : "/") + RESOURCE;
    }

    protected InputStream getContentFromResponse() throws IOException, ParseException {
        HttpClient client = new SystemDefaultHttpClient();
        HttpGet getProcessor = new HttpGet(getAPIString());
        HttpResponse processorResponse = client.execute(getProcessor);
        return processorResponse.getEntity().getContent();
    }

    public static void main(String[] args) throws Exception {
        PolarDataImporter polarDataImporter = new PolarDataImporter();
        InputStream inputStream = polarDataImporter.retrievePolarDataRegressionAsBytes();
        polarDataImporter.persistPolarDataRegressionAsBytes(new File(polarDataFilePath), inputStream);
    }

}
