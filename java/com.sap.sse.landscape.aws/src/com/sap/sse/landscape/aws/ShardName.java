package com.sap.sse.landscape.aws;

/**
 * ShardName is a wrapper class for a {@link AwsShard}'s target group name, replica set name and the shard's name.
 * {@link #parse} and create {@link #create} are used for decoding a shard's name by it's target group, replica set and
 * the target groups tag with the key {@link #TAG_KEY} and creating a {@link ShardName} by a replica set and the (user-)
 * entered shardname. Those created values should be used as names in the AWS landscape.
 * 
 * Only a-z, A-Z and 0-9 are allowed in a shard name due to encoding and there are target group length restrictions.
 * 
 * @author I569653
 *
 */

public class ShardName {
    final public static String TAG_KEY = "shardname";
    final static int POST_LENGTH = 2;
    final static int PRE_LENGTH = 2;
    final static String NAMESEPARATOR = "--";
    final static String SEPARATOR = "-";
    final String shardName;
    final String replicaName;
    final String targetGroupname;

    private ShardName(String shardName, String replicaName, String targetGroupname) {
        this.shardName = shardName;
        this.replicaName = replicaName;
        this.targetGroupname = targetGroupname;
    }
    
    /**
     * This method is supposed to convert a target group name into a {@link ShardName} via slicing the
     * name into the ReplicaSet name and the shard name. And if there is a tagString given, this is supposed to
     * be the shard's name. So it does not get parsed out of the target group name.
     * @param shardTargetGroupName
     *          name read from a target group in AWS.
     * @param tagString
     *          tag value with the key {@link TAG_KEY}
     * @return
     */
    public static ShardName parse(String shardTargetGroupName, String tagString) {
        // Possible options: S-{Replicaname}-{Shardname} or S-{Replicaname}-{Shardname[0:2]}--{Shardname[-2:0]}
        final String shardname, replicaname;
        int idxDoubleHyphen = shardTargetGroupName.indexOf(NAMESEPARATOR);
        if (tagString == null || tagString.isEmpty()) {
            final int idxHyphen1 = shardTargetGroupName.lastIndexOf(SEPARATOR);
            shardname = shardTargetGroupName.substring(idxHyphen1 + 1);
            final int idxHyphen2 = shardTargetGroupName.lastIndexOf(SEPARATOR, idxHyphen1 - 1);
            replicaname = shardTargetGroupName.substring(idxHyphen2 + 1, idxHyphen1);
        } else {
            if(idxDoubleHyphen > -1) {
                final int idxSingleHyphen = shardTargetGroupName.lastIndexOf(idxDoubleHyphen - 1);
                replicaname = shardTargetGroupName.substring(idxSingleHyphen + 1, idxDoubleHyphen);
            } else {
                final int idxHyphen1 = shardTargetGroupName.lastIndexOf(SEPARATOR);
                final int idxHyphen2 = shardTargetGroupName.lastIndexOf(SEPARATOR, idxHyphen1 - 1);
                replicaname = shardTargetGroupName.substring(idxHyphen2 + 1, idxHyphen1);
            }
            shardname = tagString;
        }
        return new ShardName(shardname, replicaname, shardTargetGroupName);
    }
    
    /**
     * Creates a ShardName from {@code replicaSetName} and {@code shardname}. {@code shardname} should only contain a-z,
     * A-Z and numbers. This function builds from those inputs a valid shard-target group name. This can have two
     * patterns: Firstly, S-{replica set name}-{shardname} or S-{relica set name}-{shard name [0:{@code PRE_LENGTH}]}--{shard name
     * [-{@code POST_LENGTH}:0]}
     * 
     * @param replicaSetName
     *            Replica set name of the shard
     * @param shardName
     *            (User-) entered shard name
     * @return
     * @throws IllegalArgumentException
     *             Gets thrown if {@code shardName} is not valid or the combination is invalid. This happens if both
     *             pattern cannot be applied.
     */
    public static ShardName create(String replicaSetName, String shardName) throws IllegalArgumentException {
        if (!shardName.matches("[a-zA-Z0-9]*")) {
            throw new IllegalArgumentException("Only a-z, A-Z and 0-9 characters are allowed in shardname!");
        }
        if (shardName.endsWith(TargetGroup.MASTER_SUFFIX) || shardName.endsWith(TargetGroup.TEMP_SUFFIX)){
            throw new IllegalArgumentException(TargetGroup.MASTER_SUFFIX + " and " + TargetGroup.TEMP_SUFFIX + " are not allowed at the end of Shardnames");
        }
        final String name;
        if (TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX.length() + 1 + replicaSetName.length() + 1
                + shardName.length() <= TargetGroup.MAX_TARGETGROUP_NAME_LENGTH) {
            name = TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX + replicaSetName + SEPARATOR + shardName;
        } else {
            if (TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX.length() + 1 + replicaSetName.length() + 1
                    + shardName.length() < TargetGroup.MAX_TARGETGROUP_NAME_LENGTH
                            - (POST_LENGTH + PRE_LENGTH + NAMESEPARATOR.length() + 1)) {
                throw new IllegalArgumentException(
                        "TargetGoup's name, in combination with this shardname, shouldn't be longer than "
                                + (TargetGroup.MAX_TARGETGROUP_NAME_LENGTH - POST_LENGTH - PRE_LENGTH
                                        - NAMESEPARATOR.length())
                                + "chars");
            }
            name = TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX + replicaSetName + SEPARATOR + shardName.substring(0, PRE_LENGTH)
                    + NAMESEPARATOR + shardName.substring(shardName.length() - POST_LENGTH -1);
        }
        return new ShardName(shardName, replicaSetName, name);
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
