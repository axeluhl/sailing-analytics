package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;

/**
 * This class adds ownership and acl information to the normal StrippedLeaderboardDTO, it should only be used by the
 * Adminconsole, as especially in auto reloading scenarios it can create significant overhead
 */
public class StrippedLeaderboardDTOWithSecurity extends StrippedLeaderboardDTO implements SecuredDTO {
    private static final long serialVersionUID = 3285029720177137625L;

    private OwnershipDTO ownership;
    private AccessControlListDTO acl;
    
    @Deprecated
    StrippedLeaderboardDTOWithSecurity() {
        super(); // for GWT serialization only
    }
    
    public StrippedLeaderboardDTOWithSecurity(BoatClassDTO boatClass) {
        super(boatClass);
    }
    
    @Override
    public AccessControlListDTO getAccessControlList() {
        return acl;
    }

    @Override
    public OwnershipDTO getOwnership() {
        return ownership;
    }

    @Override
    public void setAccessControlList(AccessControlListDTO acl) {
        this.acl = acl;
    }

    @Override
    public void setOwnership(OwnershipDTO ownership) {
        this.ownership = ownership;
    }

    @Override
    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(getName());
    }

    @Override
    public HasPermissions getType() {
        return SecuredDomainType.LEADERBOARD;
    }
}
