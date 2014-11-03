# S3 Controller

Here is a sample controller class that uploads a JPEG file from your local file system to your S3 Bucket.

Dependencies: http://aws.amazon.com/de/sdk-for-java/

    package <you package>;

    import com.amazonaws.AmazonClientException;
    import com.amazonaws.auth.AWSCredentials;
    import com.amazonaws.auth.BasicAWSCredentials;
    import com.amazonaws.services.s3.AmazonS3;
    import com.amazonaws.services.s3.AmazonS3Client;
    import com.amazonaws.services.s3.model.GetObjectRequest;
    import com.amazonaws.services.s3.model.ListObjectsRequest;
    import com.amazonaws.services.s3.model.ObjectListing;
    import com.amazonaws.services.s3.model.ObjectMetadata;
    import com.amazonaws.services.s3.model.PutObjectRequest;
    import com.amazonaws.services.s3.model.S3Object;
    import com.amazonaws.services.s3.model.S3ObjectSummary;
    import java.io.BufferedInputStream;
    import java.io.BufferedOutputStream;
    import java.io.File;
    import java.io.FileNotFoundException;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.OutputStream;

    public class S3Controller {

        public static final String s3endpoint = "https://s3-eu-west-1.amazonaws.com/";
        
        public static final String profileImageBucket = "<your bucket>";
        
        private static final String accessKey = "<S3 access key>";

        private static final String secretKey = "<S3 secret key>";
       
        // put 
        public void put(String filename, String bucketName, File file)
                throws FileNotFoundException {
            AWSCredentials myCredentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 s3 = new AmazonS3Client(myCredentials);
            PutObjectRequest putObject = new PutObjectRequest(bucketName, filename, file);
            ObjectMetadata metaData = new ObjectMetadata();
            metaData.setContentType("image/jpeg");
            metaData.setContentLength(file.length());
            putObject.setMetadata(metaData);
            s3.putObject(putObject);

        }

        // delete file
        public void delete(String fileName, String bucketName) {
            AWSCredentials myCredentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 s3 = new AmazonS3Client(myCredentials);
            s3.deleteObject(bucketName, fileName);
        }

        // get a private file to local storage
        public void get(String fileName, String bucketName, File localFile) {
            AWSCredentials myCredentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 s3 = new AmazonS3Client(myCredentials);
            try {
                S3Object object = s3.getObject(new GetObjectRequest(bucketName, fileName));
                InputStream inputStream = new BufferedInputStream(object.getObjectContent());
                try {
                    try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile))) {
                        int read = -1;
                        while ((read = inputStream.read()) != -1) {
                            outputStream.write(read);
                        }
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            } catch (AmazonClientException e) {
            }
        }

        public boolean fileExists(String fileName, String bucketName) {
            AWSCredentials myCredentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 s3 = new AmazonS3Client(myCredentials);
            ObjectListing objects = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(fileName));
            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                if (objectSummary.getKey().startsWith(fileName)) {
                    return true;
                }
            }
            return false;
        }
    }
