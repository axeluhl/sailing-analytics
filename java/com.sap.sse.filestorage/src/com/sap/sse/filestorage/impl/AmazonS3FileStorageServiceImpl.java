package com.sap.sse.filestorage.impl;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.FileStorageServiceProperty;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;

/**
 * For testing purposes configure the access credentials as follows: To link this service to an AWS account, create the
 * following file: ~/.aws/credentials and add credentials to it (get the access id and secret key from
 * https://console.aws.amazon.com/iam/home?#security_credential).
 * 
 * @author Fredrik Teschke
 * @author Axel Uhl
 *
 */
public class AmazonS3FileStorageServiceImpl extends BaseFileStorageServiceImpl implements FileStorageService {
    private static final long serialVersionUID = -2406798172882732531L;
    public static final String NAME = "Amazon S3";

    private static final Logger logger = Logger.getLogger(AmazonS3FileStorageServiceImpl.class.getName());

    private static final String retrievalProtocol = "https";
    private static final String regionRetrievalHost = "s3-eu-west-1.amazonaws.com";

    private final FileStorageServicePropertyImpl accessId = new FileStorageServicePropertyImpl("accessId", false,
            "s3AccessIdDesc");
    private final FileStorageServicePropertyImpl accessKey = new FileStorageServicePropertyImpl("accessKey", false,
            "s3AccessKeyDesc");
    private final FileStorageServicePropertyImpl bucketName = new FileStorageServicePropertyImpl("bucketName", true,
            "s3BucketNameDesc");

    public AmazonS3FileStorageServiceImpl() {
        super(NAME, "s3Desc");
        addProperties(accessId, accessKey, bucketName);
    }

    private AmazonS3Client createS3Client() throws InvalidPropertiesException {
        AWSCredentialsProvider creds;

        // first try to use properties
        if (accessId.getValue() != null && accessKey.getValue() != null) {
            creds = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessId.getValue(), accessKey.getValue()));
        } else {
            // if properties are empty, read credentials from ~/.aws/credentials
            try {
                creds = new ProfileCredentialsProvider();
            } catch (Exception e) {
                throw new InvalidPropertiesException(
                        "credentials in ~/.aws/credentials seem to be invalid (tried this as fallback because properties were empty)",
                        e);
            }
        }
        return (AmazonS3Client) AmazonS3ClientBuilder.standard().withCredentials(creds).withRegion(regionRetrievalHost).enableForceGlobalBucketAccess().build();
    }

    private static String getKey(String fileExtension) {
        String key = UUID.randomUUID().toString();
        key += fileExtension;
        return key;
    }

    private URI getUri(String key) {
        try {
            return new URI(retrievalProtocol, regionRetrievalHost, "/" + bucketName.getValue() + "/" + key, null);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Could not create URI for uploaded file with key " + key, e);
            return null;
        }
    }

    @Override
    public URI storeFile(final InputStream is, String fileExtension, long lengthInBytes)
            throws InvalidPropertiesException, OperationFailedException {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(lengthInBytes);
        final String key = getKey(fileExtension);
        // TODO bug 2583: use something like SecurityUtil.getSubject().checkPermission("file:store:"+key)
        final PutObjectRequest request = new PutObjectRequest(bucketName.getValue(), key, is, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        final AmazonS3Client s3Client = createS3Client();
        try {
            s3Client.putObject(request);
        } catch (AmazonClientException e) {
            logger.log(Level.SEVERE, "Could not store file", e);
            throw new OperationFailedException(e.getMessage(), e);
        }
        URI uri = getUri(key);
        logger.info("Stored file " + uri);
        return uri;
    }

    @Override
    public void removeFile(URI uri) throws InvalidPropertiesException, OperationFailedException {
        String key = uri.getPath().substring(uri.getPath().lastIndexOf("/")+1);
        AmazonS3Client s3Client = createS3Client();
        try {
            s3Client.deleteObject(new DeleteObjectRequest(bucketName.getValue(), key));
        } catch (AmazonClientException e) {
            throw new OperationFailedException("Could not remove file " + uri.toString(), e);
        }
        logger.info("Removed file " + uri);
    }

    @Override
    public void testProperties() throws InvalidPropertiesException {
        AmazonS3Client s3 = createS3Client();

        
        if (bucketName.getValue().equals("")) {
            throw new InvalidPropertiesException("empty bucketname is not allowed");
        }
        // test if credentials are valid
        // TODO seems to even work if credentials are not valid if bucket is publicly visible
        // Fix is probably available with doesBucketExistsV2 in a later version of amazons3 library
        try {
            s3.doesBucketExist(bucketName.getValue());
        } catch (Exception e) {
            throw new InvalidPropertiesException("invalid credentials or not enough access rights for the bucket", e,
                    new Pair<FileStorageServiceProperty, String>(accessId, "seems to be invalid"),
                    new Pair<FileStorageServiceProperty, String>(accessKey, "seems to be invalid"));
        }

        // test if bucket exists
        if (!s3.doesBucketExist(bucketName.getValue())) {
            throw new InvalidPropertiesException("invalid bucket", new Pair<FileStorageServiceProperty, String>(
                    bucketName, "bucket does not exist"));
        }
    }
}
