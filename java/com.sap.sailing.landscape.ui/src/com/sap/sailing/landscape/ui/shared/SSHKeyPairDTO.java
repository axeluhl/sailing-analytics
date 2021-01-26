package com.sap.sailing.landscape.ui.shared;

import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.common.SecuredLandscapeTypes;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.NamedSecuredObjectDTO;

public class SSHKeyPairDTO extends NamedSecuredObjectDTO {
    private static final long serialVersionUID = -9174909996567452216L;
    private String regionId;
    private String creatorName;
    private TimePoint creationTime;
    
    @Deprecated
    SSHKeyPairDTO() {} // for GWT RPC serialization only

    public SSHKeyPairDTO(String regionId, String name, String creatorName, TimePoint creationTime) {
        super(name);
        this.regionId = regionId;
        this.creatorName = creatorName;
        this.creationTime = creationTime;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public TimePoint getCreationTime() {
        return creationTime;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(getRegionId(), getName());
    }

    @Override
    public HasPermissions getPermissionType() {
        return SecuredLandscapeTypes.SSH_KEY;
    }
}
