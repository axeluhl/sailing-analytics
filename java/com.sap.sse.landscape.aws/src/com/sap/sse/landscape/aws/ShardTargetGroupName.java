package com.sap.sse.landscape.aws;

import com.sap.sse.common.Util;

/**
 * A wrapper class for an {@link AwsShard}'s target group name and the shard's name. {@link #parse} and create
 * {@link #create} are used for encoding/decoding a shard's name by it's target group, replica set name and the target
 * group tag with the key {@link #TAG_KEY} and creating a {@link ShardTargetGroupName} by a replica set and the shard
 * name. Those created values should be used as names in the AWS landscape.<p>
 * 
 * Only a-z, A-Z and 0-9 are allowed in a shard name due to encoding and there are target group length restrictions.
 * 
 * @author I569653
 *
 */
public class ShardTargetGroupName {
    public final static String TAG_KEY = "shardname";
    static final String NAMESEPARATOR = "--";
    static final String SEPARATOR = "-";
    private final String shardName;
    private final String targetGroupname;

    private ShardTargetGroupName(String shardName, String targetGroupname) {
        this.shardName = shardName;
        this.targetGroupname = targetGroupname;
    }
    
    /**
     * This method is supposed to convert a target group name into a {@link ShardTargetGroupName} via slicing the name into the
     * ReplicaSet name and the shard name. And if there is a non-{@code null} {@code tagString} given, this is supposed
     * to be the shard's name. So in that case it does not get parsed out of the target group name.
     * 
     * @param shardTargetGroupName
     *            name read from a target group in AWS.
     * @param tagString
     *            tag value with the key {@link TAG_KEY}
     * @return
     */
    public static ShardTargetGroupName parse(String shardTargetGroupName, String tagString) {
        // Possible options: S-{Replicaname}-{Shardname} or S-{Replicaname}-{Shardname-Prefix}--{ShardnameSuffix}
        final String shardname;
        if (Util.hasLength(tagString)) {
            shardname = tagString;
        } else {
            final int idxHyphen1 = shardTargetGroupName.indexOf(SEPARATOR, TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX.length());
            shardname = shardTargetGroupName.substring(idxHyphen1 + 1);
        }
        return new ShardTargetGroupName(shardname, shardTargetGroupName);
    }
    
    /**
     * Creates a ShardName from {@code replicaSetName} and {@code shardname}. {@code shardname} should only contain a-z,
     * A-Z and numbers. This function builds a valid shard target group name from those inputs. This follows one of two
     * patterns: If the {@link TargetGroup#MAX_TARGETGROUP_NAME_LENGTH} is sufficient, after the common
     * {@link TargetGroup#SAILING_TARGET_GROUP_NAME_PREFIX} the replica set name and the shard name are concatenated,
     * each separated by a single {@link #SEPARATOR} character. If the maximum target group name length is too short for
     * this pattern, only a prefix and postfix of the shard name are used, concatenated by the {@link #NAMESEPARATOR}
     * string. The prefix and postfix length are then chosen such that the maximum target group name limit is met
     * exactly. If not even a single shard name character can be used without exceeding the maximum target group name
     * length, an {@link IllegalArgumentException} is thrown.
     * 
     * @param replicaSetName
     *            Replica set name of the shard
     * @param shardName
     *            shard name
     * @return a composite {@link ShardTargetGroupName} object that has the {@code shardName} as well as the
     *         {@link ShardTargetGroupName#getTargetgroupName() target group name} resulting from the construction rules
     *         encapsulated by this class
     * @throws IllegalArgumentException
     *             Gets thrown if {@code shardName} is not valid or the combination is invalid. This happens if neither
     *             pattern can be applied.
     */
    public static ShardTargetGroupName create(String replicaSetName, String shardName) throws IllegalArgumentException {
        if (!shardName.matches("[a-zA-Z0-9]*")) {
            throw new IllegalArgumentException("Only a-z, A-Z and 0-9 characters are allowed in shardname!");
        }
        if (shardName.endsWith(TargetGroup.MASTER_SUFFIX) || shardName.endsWith(TargetGroup.TEMP_SUFFIX)){
            throw new IllegalArgumentException(TargetGroup.MASTER_SUFFIX + " and " + TargetGroup.TEMP_SUFFIX + " are not allowed at the end of Shardnames");
        }
        final String targetGroupNameBySimpleConcatenation = TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX + replicaSetName + SEPARATOR + shardName;
        final String targetGroupName;
        if (targetGroupNameBySimpleConcatenation.length() <= TargetGroup.MAX_TARGETGROUP_NAME_LENGTH) {
            targetGroupName = targetGroupNameBySimpleConcatenation;
        } else {
            final String targetGroupNamePrefixForElidedShardNameMiddle = TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX + replicaSetName + SEPARATOR;
            if (targetGroupNamePrefixForElidedShardNameMiddle.length() + NAMESEPARATOR.length() >= TargetGroup.MAX_TARGETGROUP_NAME_LENGTH) {
                throw new IllegalArgumentException(
                        "Cannot add shard name prefix/postfix to replica set name "+replicaSetName+
                        " as it would exceed the maximum target group name limit of "+TargetGroup.MAX_TARGETGROUP_NAME_LENGTH+" characters");
            }
            final int remainingCharactersAfterAddingNameSeparator = TargetGroup.MAX_TARGETGROUP_NAME_LENGTH
                    - targetGroupNamePrefixForElidedShardNameMiddle.length() - NAMESEPARATOR.length();
            final int prefixLength = remainingCharactersAfterAddingNameSeparator/2 + remainingCharactersAfterAddingNameSeparator%2; // prefer prefix in odd cases
            final int postfixLength = remainingCharactersAfterAddingNameSeparator/2;
            targetGroupName = TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX + replicaSetName + SEPARATOR + shardName.substring(0, prefixLength)
                    + NAMESEPARATOR + shardName.substring(shardName.length() - postfixLength);
        }
        return new ShardTargetGroupName(shardName, targetGroupName);
    }

    public String getName() {
        return shardName;
    }

    public String getTargetgroupName() {
        return targetGroupname;
    }

    public String getShardName() {
        return shardName;
    }

    public static boolean isValidTargetGroupName(String name) {
        boolean ret = true;
        if (name.length() > 32 || !name.startsWith(TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX) || name.endsWith(TargetGroup.MASTER_SUFFIX) || name.endsWith(TargetGroup.TEMP_SUFFIX)) {
            ret = false;
        }
        if (name.contains(NAMESEPARATOR)) {
            int idx = name.lastIndexOf(NAMESEPARATOR);
            int idx2 = name.lastIndexOf(SEPARATOR, idx - 1);
            int idx3 = name.lastIndexOf(SEPARATOR, idx2 - 1);
            if (idx2 < 2 || idx3 != 1) {
                ret = false;
            }
        }
        return ret;
    }
}
