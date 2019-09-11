package com.sap.sse.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.NamedSecuredObjectDTO;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public class ServerInfoDTO extends NamedSecuredObjectDTO implements IsSerializable {

    private static final long serialVersionUID = 554811472250094684L;

    private String buildVersion;
    
    // for GWT
    ServerInfoDTO() {
    }

    public ServerInfoDTO(String serverName, String buildVersion) {
        setName(serverName);
        this.buildVersion = buildVersion;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public static TypeRelativeObjectIdentifier getServerIdentifier(String serverName) {
        return new TypeRelativeObjectIdentifier(serverName);
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return getTypeRelativeObjectIdentifier(this.getName());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String serverName) {
        return new TypeRelativeObjectIdentifier(serverName);
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.SERVER;
    }

}
