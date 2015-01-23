package com.sap.sse.filestorage.impl;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;
import com.sap.sse.filestorage.Property;

/**
 * For testing purposes configure the access credentials as follows: To link this service to an AWS account, create the
 * following file: ~/.aws/credentials and add credentials to it (get the access id and secret key from
 * https://console.aws.amazon.com/iam/home?#security_credential).
 * 
 * TODO configure credentials in AdminConsole TODO configure bucket name in AdminConsole
 * 
 * @author Fredrik Teschke
 *
 */
public class AmazonS3FileStorageServiceImpl implements FileStorageService {
    public static final String NAME = "Amazon S3";
    private static final Logger logger = Logger.getLogger(AmazonS3FileStorageServiceImpl.class.getName());

    private static final String baseUrl = "s3.amazonaws.com";
    private static final String retrievalProtocol = "http";
    // private static final String bucketName = "ftes-sap-sailing";

    private final PropertyImpl accessId = new PropertyImpl("accessId", false,
            "Access ID (leave blank to use ~/.aws/credentials instead)");
    private final PropertyImpl accessKey = new PropertyImpl("accessKey", false,
            "Secret Access Key (leave blank to use ~/.aws/credentials instead)");
    private final PropertyImpl bucketName = new PropertyImpl("bucketName", true,
            "Name of Bucket to use (has to already exist, and user needs sufficient permissions)");
    private final Map<String, PropertyImpl> properties = new HashMap<>();

    public AmazonS3FileStorageServiceImpl() {
        addProperties(accessId, accessKey, bucketName);
    }

    private void addProperties(PropertyImpl... properties) {
        for (PropertyImpl p : properties) {
            this.properties.put(p.getName(), p);
        }
    }

    private AmazonS3Client createS3Client() throws InvalidPropertiesException {
        AWSCredentials creds;

        // first try to use properties
        if (accessId.getValue() != null && accessKey.getValue() != null) {
            creds = new BasicAWSCredentials(accessId.getValue(), accessKey.getValue());

        } else {
            // if properties are empty, read credentials from ~/.aws/credentials
            try {
                creds = new ProfileCredentialsProvider().getCredentials();
            } catch (Exception e) {
                throw new InvalidPropertiesException(
                        "credentials in ~/.aws/credentials seem to be invalid (tried this as fallback because properties were empty)",
                        e);
            }
        }
        return new AmazonS3Client(creds);
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

    private URI getUri(String key) {
        try {
            // FIXME: region is missing s3-... see: http://stackoverflow.com/questions/10975475/amazon-s3-upload-file-and-get-url
            return new URI(retrievalProtocol, baseUrl, "/" + bucketName.getValue() + "/" + key, null);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Could not create URI for uploaded file with key " + key, e);
            return null;
        }
    }

    @Override
    public URI storeFile(InputStream is, String originalFileName, long lengthInBytes)
            throws InvalidPropertiesException, OperationFailedException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(lengthInBytes);
        String key = getKey(originalFileName);
        PutObjectRequest request = new PutObjectRequest(bucketName.getValue(), key, is, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        AmazonS3Client s3Client = createS3Client();
        try {
            s3Client.putObject(request);
        } catch (AmazonClientException e) {
            throw new OperationFailedException("Could not store file " + originalFileName, e);
        }
        URI uri = getUri(key);
        logger.info("Stored file " + uri);
        return uri;
    }

    @Override
    public void removeFile(URI uri) throws InvalidPropertiesException, OperationFailedException {
        String key = uri.getPath().substring(1); // remove initial slash
        AmazonS3Client s3Client = createS3Client();
        try {
            s3Client.deleteObject(new DeleteObjectRequest(bucketName.getValue(), key));
        } catch (AmazonClientException e) {
            throw new OperationFailedException("Could not remove file " + uri.toString(), e);
        }
        logger.info("Removed file " + uri);
    }
    @Override
    public Property[] getProperties() {
        return new Property[] { accessId, accessKey, bucketName };
    }

    @Override
    public void setProperty(String name, String value) {
        if (!properties.containsKey(name)) {
            throw new IllegalArgumentException("Property " + name + " does not exist");
        }
        properties.get(name).setValue(value);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Store files on Amazon S3. The resulting file name is a random UUID plus the original file ending.";
    }

    @Override
    public void testProperties() throws InvalidPropertiesException {
        AmazonS3Client s3 = createS3Client();

        // test if credentials are valid
        // TODO seems to even work if credentials are not valid if bucket is publicly visible
        try {
            s3.doesBucketExist(bucketName.getValue());
        } catch (Exception e) {
            throw new InvalidPropertiesException("invalid credentials or not enough access rights for the bucket", e,
                    new Pair<>(accessId, "seems to be invalid"), new Pair<>(accessKey, "seems to be invalid"));
        }

        // test if bucket exists
        if (!s3.doesBucketExist(bucketName.getValue())) {
            throw new InvalidPropertiesException("invalid bucket", new Pair<>(bucketName,
                    "bucket does not exist"));
        }
    }
}
