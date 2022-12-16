package com.sap.sailing.ingestion;

/**
 * This class defines a structure to facilitate the creation of a {@link ListObjectsV2Request} to the S3. The instance
 * variables abstract the used basic structure of a {@link ListObjectsV2Request}. It defines a prefix, a key after which
 * the response should start (keys on results are UTF-8 sorted) and the max amount of keys to give in one batch.
 * 
 * @author Kevin Wiesner
 *
 */
public class S3FixStorageListRequest {
    private final String commonPrefix;
    private final String keyStartAfter;
    private final int maxKeys;

    public S3FixStorageListRequest(String commonPrefix, String keyStartAfter, int maxKeys) {
        super();
        this.commonPrefix = commonPrefix;
        this.keyStartAfter = keyStartAfter;
        this.maxKeys = maxKeys;
    }

    public String getCommonPrefix() {
        return commonPrefix;
    }

    public String getKeyStartAfter() {
        return keyStartAfter;
    }

    public int getMaxKeys() {
        return maxKeys;
    }
}
