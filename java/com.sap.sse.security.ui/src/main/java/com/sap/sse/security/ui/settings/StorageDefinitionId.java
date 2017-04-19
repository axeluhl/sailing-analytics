package com.sap.sse.security.ui.settings;

public class StorageDefinitionId {
    
    private static final String PREFIX = "sailing.ui.usersettings.";

    private final String globalId;
    private final String contextSpecificId;
    
    public StorageDefinitionId(String globalId, String contextSpecificId) {
        this.globalId = globalId;
        this.contextSpecificId = contextSpecificId;
    }
    
    public String getGlobalId() {
        return globalId;
    }
    
    public String getContextSpecificId() {
        return contextSpecificId;
    }
    
    public String generateStorageGlobalKey() {
        return PREFIX + globalId;
    }
    
    public String generateStorageContextSpecificKey() {
        return generateStorageGlobalKey() + "#" + contextSpecificId;
    }
    
    /**
     * Utility method used to build a context specific id for {@link StorageDefinitionId} construction.
     * 
     * @param contextDefinitionParameters
     *            The parameters which shape the context
     * @return The generated context definition id from the provided parameters
     */
    public static String buildContextDefinitionId(String... contextDefinitionParameters) {
        StringBuilder str = new StringBuilder("");
        boolean first = true;
        for (String contextDefinitionParameter : contextDefinitionParameters) {
            if (first) {
                first = false;
            } else {
                str.append(",");
            }
            if (contextDefinitionParameter != null) {
                str.append(contextDefinitionParameter);
            }
        }
        return str.toString();
    }

}
