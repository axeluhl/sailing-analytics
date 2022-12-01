package com.sap.sailing.landscape.ui.shared;

public class ShardNameDTO {
    final static String TAG_KEY = "shardname";
    
    String shardName;
    String replicaName;
    String tagValue;
    
    public String getName(String replicaName, String Shardname) {
        return "";
    }
    
    public static ShardNameDTO parse(String shardTargetName) {
        return new ShardNameDTO();
    }
}
