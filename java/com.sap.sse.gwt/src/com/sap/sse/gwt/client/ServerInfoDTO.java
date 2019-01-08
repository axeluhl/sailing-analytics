package com.sap.sse.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public class ServerInfoDTO implements IsSerializable, WithQualifiedObjectIdentifier
{
    private static final long serialVersionUID = 4456475715605308221L;

    private String serverName;
    private String buildVersion;
    
    // for GWT
    ServerInfoDTO() {
    }

    public ServerInfoDTO(String serverName, String buildVersion) {
        this.serverName = serverName;
        this.buildVersion = buildVersion;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public String getServerName() {
        return serverName;
    }

    public static TypeRelativeObjectIdentifier getServerIdentifier(String serverName) {
        return new TypeRelativeObjectIdentifier(serverName);
    }

    @Override
    public String getName() {
        return serverName;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return getTypeRelativeObjectIdentifier(this.serverName);
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String serverName) {
        return new TypeRelativeObjectIdentifier(serverName);
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.SERVER;
    }
}
