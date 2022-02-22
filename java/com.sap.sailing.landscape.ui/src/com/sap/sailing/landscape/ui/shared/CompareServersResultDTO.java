package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.server.gateway.interfaces.CompareServersResult;

/**
 * A GWT serializable version of {@link CompareServersResult} that provides the differences of both
 * servers as GWT-compatible {@link JSONObject} instances.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompareServersResultDTO implements IsSerializable {
    private String serverAName;
    private String serverBName;
    private String aDiffJson;
    private String bDiffJson;
    
    @Deprecated
    CompareServersResultDTO() {} // for GWT serialization only

    public CompareServersResultDTO(String serverAName, String serverBName, String aDiffJson, String bDiffJson) {
        super();
        this.serverAName = serverAName;
        this.serverBName = serverBName;
        this.aDiffJson = aDiffJson;
        this.bDiffJson = bDiffJson;
    }

    public String getServerAName() {
        return serverAName;
    }

    public String getServerBName() {
        return serverBName;
    }

    public JSONObject getADiffs() {
        return (JSONObject) JSONParser.parseStrict(aDiffJson);
    }

    public JSONObject getBDiffs() {
        return (JSONObject) JSONParser.parseStrict(bDiffJson);
    }
    
    public boolean hasDiffs() {
        return getADiffs().size() != 0 || getBDiffs().size() != 0;
    }
}
