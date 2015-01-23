package com.sap.sse.filestorage.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.Property;

public class LocalFileStorageServiceImpl implements FileStorageService {
    private static final Logger logger = Logger.getLogger(LocalFileStorageServiceImpl.class.getName());

    private static final String host = "media.sapsailing.com";
    private static final String path = "images";
    private static final String retrievalProtocol = "http";

    @Override
    public URI storeFile(InputStream is, String originalFileName, long lengthInBytes) throws IOException {
        OutputStream outputStream = null;
        String pathToFile = path + "/" + getKey(originalFileName);

        outputStream = new FileOutputStream(new File(pathToFile));

        try {
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

        } finally {
            if (is != null) {
                is.close();
            }
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }

        return getUri(pathToFile);
    }

    private static String getKey(String originalFileName) {
        String key = UUID.randomUUID().toString();
        if (originalFileName != null) {
            String ending = originalFileName.substring(originalFileName.lastIndexOf("."));
            key += ending;
            // key += "/" + originalFileName;
        }
        return key;
    }

    private static URI getUri(String pathToFile) {
        try {
            return new URI(retrievalProtocol, host, pathToFile, null);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Could not create URI for uploaded file with path " + pathToFile, e);
            return null;
        }
    }

    @Override
    public void removeFile(URI uri) {
        String filePath = uri.getPath();
        File file = new File(filePath);
        
        if(!file.delete()){
            logger.warning("Could not delete file with path "+filePath);
        }
    }

    @Override
    public Property[] getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProperty(String name, String value) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void testProperties() throws InvalidPropertiesException {
        // TODO Auto-generated method stub
        
    }

}