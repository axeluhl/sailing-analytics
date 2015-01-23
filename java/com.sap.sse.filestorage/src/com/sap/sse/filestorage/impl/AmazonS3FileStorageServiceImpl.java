package com.sap.sse.filestorage.impl;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sap.sse.filestorage.FileStorageService;

/**
 * For testing purposes configure the access credentials as follows:
 * To link this service to an AWS account, create the following file: ~/.aws/credentials
 * and add credentials to it (get the access id and secret key from
 * https://console.aws.amazon.com/iam/home?#security_credential).
 * 
 * TODO configure credentials in AdminConsole
 * TODO configure bucket name in AdminConsole
 * 
 * @author Fredrik Teschke
 *
 */
public class AmazonS3FileStorageServiceImpl implements FileStorageService {
    private static final Logger logger = Logger.getLogger(AmazonS3FileStorageServiceImpl.class.getName());
    
    private static final String baseUrl = "s3.amazonaws.com";
    private static final String retrievalProtocol = "http";
    private static final String bucketName = "ftes-sap-sailing";
    private final AmazonS3 s3;
    
    public AmazonS3FileStorageServiceImpl() {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        s3 = new AmazonS3Client(credentials);
    }
    
    private static String getKey(String originalFileName) {
        String key = UUID.randomUUID().toString();
        if (originalFileName != null) {
            String ending = originalFileName.substring(originalFileName.lastIndexOf("."));
            key += ending;
//            key += "/" + originalFileName;
        }
        return key;
    }
    
    private static URI getUri(String key) {
        try {
            return new URI(retrievalProtocol, bucketName + "." + baseUrl, "/" + key, null);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Could not create URI for uploaded file with key " + key, e);
            return null;
        }
    }


    @Override
    public URI storeFile(InputStream is, String originalFileName, long lengthInBytes) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(lengthInBytes);
        String key = getKey(originalFileName);
        PutObjectRequest request = new PutObjectRequest(bucketName, key, is, metadata)
            .withCannedAcl(CannedAccessControlList.PublicRead);
        s3.putObject(request);
        URI uri = getUri(key);
        logger.info("Stored file " + uri);
        return uri;
    }

    @Override
    public void removeFile(URI uri) {
        String key = uri.getPath().substring(1); //remove initial slash
        s3.deleteObject(new DeleteObjectRequest(bucketName, key));
        logger.info("Removed file " + uri);
    }

}
