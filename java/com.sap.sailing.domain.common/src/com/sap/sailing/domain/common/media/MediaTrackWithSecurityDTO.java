package com.sap.sailing.domain.common.media;

import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class MediaTrackWithSecurityDTO extends MediaTrack implements SecuredDTO {
    private static final long serialVersionUID = 1907871403892921019L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    @Deprecated
    MediaTrackWithSecurityDTO() {
        super();
    }

    public MediaTrackWithSecurityDTO(MediaTrack mediaTrack) {
        super(mediaTrack.dbId, mediaTrack.title, mediaTrack.url, mediaTrack.startTime, mediaTrack.duration,
                mediaTrack.mimeType, mediaTrack.assignedRaces);
    }

    @Override
    public AccessControlListDTO getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public OwnershipDTO getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public void setAccessControlList(AccessControlListDTO createAccessControlListDTO) {
        securityInformation.setAccessControlList(createAccessControlListDTO);
    }

    @Override
    public void setOwnership(OwnershipDTO createOwnershipDTO) {
        securityInformation.setOwnership(createOwnershipDTO);
    }
}
