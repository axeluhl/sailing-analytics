package com.sap.sse.landscape.aws;

public class ShardNameDTO {
    final public static String TAG_KEY = "shardname";
    final static int POST_LENGTH = 2;
    final static int PRE_LENGTH = 2;
    final static String SEPERATOR  = "--";
    final String shardName;
    final String replicaName;
    final String targetGroupname;
    private ShardNameDTO(String shardName, String replicaName, String targetGroupname){
        this.shardName  =shardName;
        this.replicaName = replicaName;
        this.targetGroupname = targetGroupname;
    }
    
    public static ShardNameDTO parse(String shardTargetName, String tagString) throws Exception{
        // Basic value S-{Replicaname}-{Shardname} or S-{Replicaname}-{Shardname[0:2]}--{Shardname[-2:0]}
        int idxDoubleHyphen = shardTargetName.indexOf("--");
        final String shardname, replicaname;
        if(idxDoubleHyphen < 0) {
            //option 1
            final int idxHyphen1 = shardTargetName.lastIndexOf("-");
            shardname  = shardTargetName.substring(idxHyphen1+1);
            final int idxHyphen2 = shardTargetName.lastIndexOf('-', idxHyphen1 -1);
            replicaname = shardTargetName.substring(idxHyphen2 + 1, idxHyphen1);
            if(tagString != null && !shardname.equals(shardname)) {
                throw new Exception("Targetgroup's shardname ("+ shardname +") does not match tag's shardname (" + tagString +")");
            }
        } else {
            final int idxSingleHyphen = shardTargetName.lastIndexOf(idxDoubleHyphen-1);
            replicaname = shardTargetName.substring(idxSingleHyphen + 1, idxDoubleHyphen);
            shardname = tagString;
        }
        return new ShardNameDTO(shardname, replicaname, shardTargetName);
    }
    
    public static ShardNameDTO create(String replicaSetName, String shardname) throws Exception {
        final String name;
        // "S-" "-" "-"
        if (TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX.length() + 1 + replicaSetName.length() + 1
                + shardname.length() < TargetGroup.MAX_TARGETGROUP_NAME_LENGTH)
        {
            name = TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX + replicaSetName + "-" + shardname;

        } else {
            if (TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX.length() + 1 + replicaSetName.length() + 1
                    + shardname.length() < TargetGroup.MAX_TARGETGROUP_NAME_LENGTH
                            - (POST_LENGTH + PRE_LENGTH + SEPERATOR.length() + 1)) {
                throw new Exception(
                        "TargetGoup's name, in combination with this shardname, shouldn't be longer than 25 chars");
            }
            name = TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX + replicaSetName + "-" + shardname.substring(0,2) + "--"  + shardname.substring(shardname.length()-3);
        }
        return new ShardNameDTO(shardname, replicaSetName, name);
    }
    
    public String getName() {
        return shardName;
    }
    
    public String getTargetgroupName() {
        return targetGroupname;
    }
    
    public static boolean isValidTargetGroupName(String name) {
        boolean ret = true;
        if(
                name.length() >32 
                ||
                !name.startsWith(TargetGroup.SAILING_TARGET_GROUP_NAME_PREFIX)
                ) {
            ret = false;
        }
        if(name.contains("--")) {
            int idx = name.lastIndexOf("--");
            int idx2 = name.lastIndexOf("-", idx - 1);
            int idx3 = name.lastIndexOf("-", idx2 -1);
            if(idx2 < 2 || idx3 != 1) {
                ret = false;
            }
        }
        return ret;
        
        
    }
}

