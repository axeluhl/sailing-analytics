package com.sap.sailing.expeditionconnector;

import java.util.UUID;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class ExpeditionDeviceConfiguration extends NamedImpl implements SecuredDTO {
    private static final long serialVersionUID = -7819154195403387909L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();
    private final UUID deviceUuid;
    
    /**
     * The ID listed as first element in a UDP message coming from Expedition, prefixed by a '#'
     * character. If {@code null}, no mapping currently exists for the device.
     */
    private final Integer expeditionBoatId;

    public ExpeditionDeviceConfiguration(String name, UUID deviceUuid, Integer expeditionBoatId) {
        super(name);
        this.deviceUuid = deviceUuid;
        this.expeditionBoatId = expeditionBoatId;
    }

    public UUID getDeviceUuid() {
        return deviceUuid;
    }

    public Integer getExpeditionBoatId() {
        return expeditionBoatId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((deviceUuid == null) ? 0 : deviceUuid.hashCode());
        result = prime * result + ((expeditionBoatId == null) ? 0 : expeditionBoatId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExpeditionDeviceConfiguration other = (ExpeditionDeviceConfiguration) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        if (deviceUuid == null) {
            if (other.deviceUuid != null)
                return false;
        } else if (!deviceUuid.equals(other.deviceUuid))
            return false;
        if (expeditionBoatId == null) {
            if (other.expeditionBoatId != null)
                return false;
        } else if (!expeditionBoatId.equals(other.expeditionBoatId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExpeditionDeviceConfiguration [deviceUuid=" + deviceUuid + ", expeditionBoatId=" + expeditionBoatId
                + ", getName()=" + getName() + "]";
    }

    public HasPermissions getType() {
        return SecuredDomainType.EXPEDITION_DEVICE_CONFIGURATION;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    @Override
    public HasPermissions getPermissionType() {
        return SecuredDomainType.EXPEDITION_DEVICE_CONFIGURATION;
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(getDeviceUuid().toString());
    }

    @Override
    public final AccessControlListDTO getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public final OwnershipDTO getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public final void setAccessControlList(final AccessControlListDTO accessControlList) {
        this.securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public final void setOwnership(final OwnershipDTO ownership) {
        this.securityInformation.setOwnership(ownership);
    }

}
