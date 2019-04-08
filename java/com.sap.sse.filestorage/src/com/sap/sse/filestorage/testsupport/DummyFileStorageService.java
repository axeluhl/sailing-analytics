package com.sap.sse.filestorage.testsupport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.shiro.authz.UnauthorizedException;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;
import com.sap.sse.filestorage.impl.BaseFileStorageServiceImpl;
import com.sap.sse.filestorage.impl.FileStorageServicePropertyImpl;

public class DummyFileStorageService extends BaseFileStorageServiceImpl implements FileStorageService {
    public static final String NAME = "dummy";
    public static final String PROPERTY_NAME = "p";
    public final FileStorageServicePropertyImpl property = new FileStorageServicePropertyImpl(PROPERTY_NAME, false, "");

    private static final long serialVersionUID = -3871744982404841496L;

    public DummyFileStorageService() {
        super(NAME, "");
        addProperties(property);
    }

    @Override
    public URI storeFile(InputStream is, String fileExtension, long lengthInBytes) throws IOException,
            OperationFailedException, InvalidPropertiesException {
        try {
            return new URI("http://no.where");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void removeFile(URI uri) throws OperationFailedException, InvalidPropertiesException {
    }

    @Override
    public void testProperties() throws InvalidPropertiesException {
    }

    @Override
    public void doPermissionCheckForGetFile(URI uri) throws UnauthorizedException {
    }

}
