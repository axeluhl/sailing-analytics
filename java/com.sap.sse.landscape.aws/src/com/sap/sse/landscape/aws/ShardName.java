package com.sap.sse.landscape.aws;

/**
 * ShardName is a wrapper class for a {@code AwsShard}'s target group name, replica set name and the shard's name.
 * {@code parse} and create {@code create} are used for decoding a shard's name by it's target group, replica set and
 * the target groups tag with the key {@code TAG_KEY} and creating a {@code ShardName} by a replica set and the (user-)
 * entered shardname. Those created values should be used as names in the AWS landscape.
 * 
 * @author I569653
 *
 */

public class ShardName {
    final public static String TAG_KEY = "shardname";
    final static int POST_LENGTH = 2;
    final static int PRE_LENGTH = 2;
    final static String SEPERATOR = "--";
    final String shardName;
    final String replicaName;
    final String targetGroupname;

    private ShardName(String shardName, String replicaName, String targetGroupname) {
        this.shardName = shardName;
        this.replicaName = replicaName;
        this.targetGroupname = targetGroupname;
    }

    public static ShardName parse(String shardTargetName, String tagString) throws Exception {
        // Possible options: S-{Replicaname}-{Shardname} or S-{Replicaname}-{Shardname[0:2]}--{Shardname[-2:0]}
        int idxDoubleHyphen = shardTargetName.indexOf("--");
        final String shardname, replicaname;
        if (idxDoubleHyphen < 0) {
            final int idxHyphen1 = shardTargetName.lastIndexOf("-");
            shardname = shardTargetName.substring(idxHyphen1 + 1);
            final int idxHyphen2 = shardTargetName.lastIndexOf('-', idxHyphen1 - 1);
            replicaname = shardTargetName.substring(idxHyphen2 + 1, idxHyphen1);
            if (tagString != null && !shardname.equals(shardname)) {
                throw new Exception("Targetgroup's shardname (" + shardname + ") does not match tag's shardname ("
                        + tagString + ")");
            }
        } else {
            final int idxSingleHyphen = shardTargetName.lastIndexOf(idxDoubleHyphen - 1);
            replicaname = shardTargetName.substring(idxSingleHyphen + 1, idxDoubleHyphen);
            shardname = tagString;
        }
        return new ShardName(shardname, replicaname, shardTargetName);
    }
    
/**
 * Creates a ShardName from {@code replicaSetName} and {@code shardname}. {@code shardname} should only contain a-z, A-Z and numbers.
 * This function builds from those im puts a valid shard-target group name. This can have two patterns: Firstly, S-{replica set name}-{shardname}
 * or S-{relica set name}-{shard name [0:2]}--{shard name [-2:0]}
 * @param replicaSetName
        Replica set name of the shard
 * @param shardname
 *      (User-) entered shard name
 * @return
 * @throws Exception
 *      Gets thrown if {@code shardName} is not valid or the combination is invalid. This happens if both pattern cannot be applied.
 */
    public static ShardName create(String replicaSetName, String shardName) throws Exception {
        if (!shardName.matches("[a-zA-Z0-9]*")) {
            throw new Exception("Only a-z, A-Z and 0-9 characters are allowed in shardname!");
        }
        if(shardName.endsWith(TargetGroup.MASTER_SUFFIX) || shardName.endsWith(TargetGroup.TEMP_SUFFIX)){
            throw new Exception(TargetGroup.MASTER_SUFFIX + " and " + TargetGroup.TEMP_SUFFIX + " are not allowed at the end of Shardnames");
        }
        final String name;
        if (TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX.length() + 1 + replicaSetName.length() + 1
                + shardName.length() < TargetGroup.MAX_TARGETGROUP_NAME_LENGTH) {
            name = TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX + replicaSetName + "-" + shardName;
        } else {
            if (TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX.length() + 1 + replicaSetName.length() + 1
                    + shardName.length() < TargetGroup.MAX_TARGETGROUP_NAME_LENGTH
                            - (POST_LENGTH + PRE_LENGTH + SEPERATOR.length() + 1)) {
                throw new Exception(
                        "TargetGoup's name, in combination with this shardname, shouldn't be longer than 25 chars");
            }
            name = TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX + replicaSetName + "-" + shardName.substring(0, 2)
                    + "--" + shardName.substring(shardName.length() - 3);
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
        if (name.contains("--")) {
            int idx = name.lastIndexOf("--");
            int idx2 = name.lastIndexOf("-", idx - 1);
            int idx3 = name.lastIndexOf("-", idx2 - 1);
            if (idx2 < 2 || idx3 != 1) {
                ret = false;
            }
        }
        return ret;

    }
}
