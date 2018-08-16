package com.sap.sse.filestorage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.FileStorageServiceProperty;
import com.sap.sse.filestorage.InvalidPropertiesException;

/**
 * Service for storing files in the local file system. Files get stored in localPath+fileName and can be accessed at
 * baseUrl+fileName Note that the content lying in baseUrl must therefore be accessible remotely. This can for example
 * be achieved by mounting a remote file system for example from static.sapsailing.com on the replicas to localPath.
 * 
 * The accessibility of the files is ensured prior to activating the local file storage service by saving a testfile in
 * localPath, which is then accessed via basePath.
 * 
 * For testing purposes the baseUrl can be set to a file url: e.g. file://localhost/home/jan/sailing_test
 * and the local path subsequently: /home/jan/sailing_test
 * 
 * @author Jan Broß
 *
 */


public class LocalFileStorageServiceImpl extends BaseFileStorageServiceImpl implements FileStorageService {
    private static final long serialVersionUID = -8661781258137340835L;
    private static final String testFile = "Bundesliga2014_Regatta6_eventteaser.jpg";
    public static final String NAME = "Local Storage";
    
    private static final Logger logger = Logger.getLogger(LocalFileStorageServiceImpl.class.getName());

    private final FileStorageServicePropertyImpl baseURL = new FileStorageServicePropertyImpl("baseURL", true, "localBaseUrlDesc");
    private final FileStorageServicePropertyImpl localPath = new FileStorageServicePropertyImpl("localPath", true, "localLocalPathDesc");

    
    protected LocalFileStorageServiceImpl() {
        super(NAME, "localDesc");
        addProperties(baseURL, localPath);
    }

    @Override
    public URI storeFile(InputStream is, String fileExtension, long lengthInBytes) throws IOException {
        OutputStream outputStream = null;
        String fileName = getKey(fileExtension);
        String pathToFile = localPath.getValue() + "/" + fileName;
        // TODO bug 2583: use something like SecurityUtil.getSubject().checkPermission("file:store:"+pathToFile)

        File outputFile = new File(pathToFile);
        logger.log(Level.FINE, "Storing file in " + outputFile.getAbsolutePath());
        outputStream = new FileOutputStream(outputFile);

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

        return getUri(fileName);
    }
    
    private static String getKey(String fileEnding) {
        String key = UUID.randomUUID().toString();
        key += fileEnding;
        return key;
    }

    private URI getUri(String pathToFile) {
        try {
            return new URI(baseURL.getValue() + "/" + pathToFile);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Could not create URI for uploaded file with path " + pathToFile, e);
            return null;
        }
    }

    @Override
    public void removeFile(URI uri) throws IOException {
        String filePath = uri.getPath();
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        File file = new File(localPath.getValue() + "/" + fileName);
        if (!file.exists()) {
            throw new FileNotFoundException(uri.toString());
        }
        if (!file.delete()) {
            logger.warning("Could not delete file with path " + filePath);
            throw new IOException("Could not delete file with path "+filePath);
        }
    }

    @Override
    public void internalSetProperty(String name, String value) {
        // as all properties are paths, we can remove the trailing / here, if it exists
        value = removeTrailingSlash(value);
        super.internalSetProperty(name, value);
    }

    @Override
    public void testProperties() throws InvalidPropertiesException, IOException {
        // write file to localPath and read file via http operation and check content
        URI testFileURI;
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(testFile);
            long length = stream.available();
            testFileURI = storeFile(stream, ".test", length);
        } catch (IOException e) {
            throw new InvalidPropertiesException("Could not write test file", new Pair<FileStorageServiceProperty, String>(localPath,
                    "incorrect path or not writeable"));
        }

        InputStream downloadStream;
        try {
            downloadStream = testFileURI.toURL().openStream();
        } catch (Exception e) {
            throw new InvalidPropertiesException("Could not open stream to " + testFileURI,  new Pair<FileStorageServiceProperty, String>(baseURL,
                    "a test file uploaded to localpath can't be found on baseUrl"));
        }

        InputStream originalInput = getClass().getClassLoader().getResourceAsStream(testFile);
        try {
            IOUtils.contentEquals(downloadStream, originalInput);
        } catch (IOException e) {
            throw new InvalidPropertiesException("Could not compare original file with uploaded file", new Pair<FileStorageServiceProperty, String>(baseURL,
                    "unknown error"), new Pair<FileStorageServiceProperty, String>(localPath,
                            "unknown error"));
        } finally {
            try {
                downloadStream.close();
                originalInput.close();
            } catch (IOException e) {
                logger.warning("Closing Streams failed.");
            }
        }

        removeFile(testFileURI);
    }

    private String removeTrailingSlash(String value) {
        if (value != null && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}