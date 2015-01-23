package com.sap.sse.filestorage.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;
import com.sap.sse.filestorage.testsupport.AmazonS3TestSupport;

public class AmazonS3Test {
   @Before
    public void setup() throws InvalidPropertiesException {
        storageService = AmazonS3TestSupport.createService();
    }
    
    private static final String teamImageFile = "Bundesliga2014_Regatta6_eventteaser.jpg";
    private FileStorageService storageService;
    
    @Test
    public void testStoreAndRemoveFileTest() throws URISyntaxException, IOException, OperationFailedException, InvalidPropertiesException {
        URL fileUrl = getClass().getResource("/" + teamImageFile);
        URI fileUri = new URI(fileUrl.toString());
        long length = new File(fileUri).length();
        InputStream stream = getClass().getResourceAsStream("/" + teamImageFile);

        URI uri = storageService.storeFile(stream, teamImageFile, length);
        
        InputStream downloadStream = uri.toURL().openStream();
        stream = getClass().getResourceAsStream("/" + teamImageFile);
        IOUtils.contentEquals(downloadStream, stream);
        
        storageService.removeFile(uri);
    }
}
