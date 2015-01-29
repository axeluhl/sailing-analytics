package com.sap.sse.filestorage.testsupport;

import com.sap.sse.filestorage.common.InvalidPropertiesException;
import com.sap.sse.filestorage.impl.AmazonS3FileStorageServiceImpl;

public class AmazonS3TestSupport {
    public static final String s3AccessId = "AKIAJOX7PZ6ACI2FQU4A";
    public static final String s3AccessKey = "NkijH2DfhWgb9fmESPjpeIbpUF+tC220KyTOfvGJ";
    private static final String s3BucketName = "sapsailing-automatic-upload-test";
    
    public static AmazonS3FileStorageServiceImpl createService() throws InvalidPropertiesException {
        AmazonS3FileStorageServiceImpl service = new AmazonS3FileStorageServiceImpl();
        service.internalSetProperty("accessId", s3AccessId);
        service.internalSetProperty("accessKey", s3AccessKey);
        service.internalSetProperty("bucketName", s3BucketName);
        service.testProperties();
        return service;
    }
}
