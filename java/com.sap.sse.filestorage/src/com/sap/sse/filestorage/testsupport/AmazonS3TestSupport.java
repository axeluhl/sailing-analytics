package com.sap.sse.filestorage.testsupport;

import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.impl.AmazonS3FileStorageServiceImpl;
import com.sap.sse.security.SecurityService;

public class AmazonS3TestSupport {
    public static final String s3AccessId = "AKIAJOX7PZ6ACI2FQU4A";
    public static final String s3AccessKey = "NkijH2DfhWgb9fmESPjpeIbpUF+tC220KyTOfvGJ";
    private static final String s3BucketName = "sapsailing-automatic-upload-test";
    
    public static AmazonS3FileStorageServiceImpl createService(final SecurityService securityService) throws InvalidPropertiesException {
        AmazonS3FileStorageServiceImpl service = new AmazonS3FileStorageServiceImpl(/* bundleContext */ null) {
            private static final long serialVersionUID = 6887160074291578082L;

            @Override
            protected SecurityService getSecurityService() {
                return securityService;
            }
        };
        service.internalSetProperty("accessId", s3AccessId);
        service.internalSetProperty("accessKey", s3AccessKey);
        service.internalSetProperty("bucketName", s3BucketName);
        service.testProperties();
        return service;
    }
}
