package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.User;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class AccountWithSecurityDTO implements Account, SecuredDTO {
    private static final long serialVersionUID = 176992188692729118L;
    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();
    private User user;

    public AccountWithSecurityDTO(User user) {
        this.user = user;
    }

    public AccountWithSecurityDTO() {
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
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
    public void setAccessControlList(AccessControlListDTO accessControlList) {
        securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public void setOwnership(OwnershipDTO ownership) {
        securityInformation.setOwnership(ownership);
    }

}
