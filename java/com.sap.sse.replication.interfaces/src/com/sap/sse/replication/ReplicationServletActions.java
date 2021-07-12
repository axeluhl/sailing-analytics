package com.sap.sse.replication;

public interface ReplicationServletActions {
    String REPLICATION_SERVLET_BASE_PATH = "/replication/replication";
    String ACTION_PARAMETER_NAME = "action";
    String SERVER_UUID_PARAMETER_NAME = "uuid";
    String PORT_NAME = "port";
    String ADDITIONAL_INFORMATION_PARAMETER_NAME = "additional";
    String REPLICABLES_IDS_AS_STRINGS_COMMA_SEPARATED_PARAMETER_NAME = "replicaIdsAsStringsCommaSeparated";
    String REPLICABLE_ID_AS_STRING_PARAMETER_NAME = "replicaIdAsString";
    
    /**
     * Actions understood by the replication servlet
     * 
     * @author Axel Uhl (D043530)
     *
     */
    public enum Action { REGISTER, INITIAL_LOAD, DEREGISTER, STATUS, STOP_REPLICATING }
}
