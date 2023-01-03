package com.sap.sse.landscape.aws.common.shared;

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
     * This method is supposed to convert a target group name into a {@link ShardTargetGroupName} via slicing the name
     * into the replica set name and the shard name. And if there is a non-{@code null} {@code tagString} given, this is
     * supposed to be the shard's name. So in that case it does not get parsed out of the target group name.
     * 
     * @param shardTargetGroupName
     *            name read from a target group in AWS.
     * @param tagString
     *            tag value with the key {@link TAG_KEY}
     */
    public static ShardTargetGroupName parse(String shardTargetGroupName, String tagString) {
        // Possible options: {SomeOptionalPrefix}{ReplicaSetName}-{Shardname} or {SomeOptionalPrefix}{ReplicaSetName}-{Shardname-Prefix}--{ShardnameSuffix}
        // where the prefix/suffix must neither contain the SEPARATOR nor the NAMESEPARATOR
        assert isValidShardTargetGroupName(shardTargetGroupName);
        final String shardname;
        if (Util.hasLength(tagString)) {
            shardname = tagString;
        } else {
            final int lastIndexExclusiveToSearchForSeparator =
                    shardTargetGroupName.contains(NAMESEPARATOR) ? shardTargetGroupName.indexOf(NAMESEPARATOR) : shardTargetGroupName.length();
            final int idxHyphen1 = shardTargetGroupName.substring(0, lastIndexExclusiveToSearchForSeparator).lastIndexOf(SEPARATOR);
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
     * @param targetGroupNamePrefix
     *            a prefix prepended to the {@code replicaSetName}; must not be {@code null} but may be an empty string
     * @return a composite {@link ShardTargetGroupName} object that has the {@code shardName} as well as the
     *         {@link ShardTargetGroupName#getTargetGroupName() target group name} resulting from the construction rules
     *         encapsulated by this class
     * @throws IllegalArgumentException
     *             Gets thrown if {@code shardName} is not valid or the combination is invalid. This happens if neither
     *             pattern can be applied.
     */
    public static ShardTargetGroupName create(String replicaSetName, String shardName, String targetGroupNamePrefix) throws IllegalArgumentException {
        if (!shardName.matches("[a-zA-Z0-9]*")) {
            throw new IllegalArgumentException("Only a-z, A-Z and 0-9 characters are allowed in shardname!");
        }
        if (shardName.endsWith(TargetGroupConstants.MASTER_SUFFIX) || shardName.endsWith(TargetGroupConstants.TEMP_SUFFIX)){
            throw new IllegalArgumentException(TargetGroupConstants.MASTER_SUFFIX + " and " + TargetGroupConstants.TEMP_SUFFIX + " are not allowed at the end of Shardnames");
        }
        final String targetGroupNameBySimpleConcatenation = targetGroupNamePrefix + replicaSetName + SEPARATOR + shardName;
        final String targetGroupName;
        if (targetGroupNameBySimpleConcatenation.length() <= TargetGroupConstants.MAX_TARGETGROUP_NAME_LENGTH) {
            targetGroupName = targetGroupNameBySimpleConcatenation;
        } else {
            final String targetGroupNamePrefixForElidedShardNameMiddle = targetGroupNamePrefix + replicaSetName + SEPARATOR;
            if (targetGroupNamePrefixForElidedShardNameMiddle.length() + NAMESEPARATOR.length() >= TargetGroupConstants.MAX_TARGETGROUP_NAME_LENGTH) {
                throw new IllegalArgumentException(
                        "Cannot add shard name prefix/postfix to replica set name "+replicaSetName+
                        " as it would exceed the maximum target group name limit of "+TargetGroupConstants.MAX_TARGETGROUP_NAME_LENGTH+" characters");
            }
            final int remainingCharactersAfterAddingNameSeparator = TargetGroupConstants.MAX_TARGETGROUP_NAME_LENGTH
                    - targetGroupNamePrefixForElidedShardNameMiddle.length() - NAMESEPARATOR.length();
            final int prefixLength = remainingCharactersAfterAddingNameSeparator/2 + remainingCharactersAfterAddingNameSeparator%2; // prefer prefix in odd cases
            final int postfixLength = remainingCharactersAfterAddingNameSeparator/2;
            targetGroupName = targetGroupNamePrefix + replicaSetName + SEPARATOR + shardName.substring(0, prefixLength)
                    + NAMESEPARATOR + shardName.substring(shardName.length() - postfixLength);
        }
        return new ShardTargetGroupName(shardName, targetGroupName);
    }

    public String getName() {
        return shardName;
    }

    public String getTargetGroupName() {
        return targetGroupname;
    }

    public String getShardName() {
        return shardName;
    }

    /**
     * Checks whether {@code prefix} is a valid prefix for a target group name. For this, the prefix must not contain
     * the {@link #NAMESEPARATOR} and its length must be short enough that there is at least one letter remaining for
     * the replica set name and one letter for the shard name, including the {@link #SEPARATOR} used to separate the
     * replica set name from the shard name. See also {@link TargetGroupConstants#MAX_TARGETGROUP_NAME_LENGTH}.
     */
    public static boolean isValidTargetGroupNamePrefix(String prefix) {
        return !prefix.contains(NAMESEPARATOR)
                && prefix.length() + 1 + SEPARATOR.length() + 1 <= TargetGroupConstants.MAX_TARGETGROUP_NAME_LENGTH;
    }
    
    /**
     * Checks {@code name} for being a valid name for a shard's target group. The name must neither end with the
     * {@link TargetGroupConstants#MASTER_SUFFIX suffix used by the "master" target group} nor with the
     * {@link TargetGroupConstants#TEMP_SUFFIX suffix for temporary target groups}. Furthermore, the name is expected to
     * contain at least one {@link #SEPARATOR} that separates the replica set name from the shard name; and if the shard
     * name had to be shortened into a prefix / suffix separated by {@link #NAMESEPARATOR} then that prefix/suffix
     * separator needs to come after the last single occurrence of the {@link #SEPARATOR} separating the shard name from
     * the replica set name.
     * <p>
     * 
     * No assumptions are made here about any target group name prefix which may itself contain a {@link #SEPARATOR} but
     * not a {@link #NAMESEPARATOR} character. See also {@link #isValidTargetGroupNamePrefix}.
     */
    public static boolean isValidShardTargetGroupName(String name) {
        return name.length() <= TargetGroupConstants.MAX_TARGETGROUP_NAME_LENGTH
                && !name.endsWith(TargetGroupConstants.MASTER_SUFFIX)
                && !name.endsWith(TargetGroupConstants.TEMP_SUFFIX)
                && name.contains(SEPARATOR)
                && (!name.contains(NAMESEPARATOR) || name.indexOf(SEPARATOR, name.indexOf(NAMESEPARATOR)+NAMESEPARATOR.length()) < 0);
    }
}
