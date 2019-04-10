package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class SwissTimingConfigurationWithSecurityDTO implements IsSerializable, SecuredDTO {
    private static final long serialVersionUID = 8896994399042048620L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    private String name;
    private String jsonUrl;
    private String hostname;
    private Integer port;
    private String creatorName;
    
    /**
     * The URL to use for updates fed back to the SwissTiming server, such as changes of race timings
     * or course updates.
     */
    private String updateURL;
    
    /**
     * The username to use as part of the credentials for requests to the {@link #updateURL}.
     */
    private String updateUsername;

    /**
     * The password to use as part of the credentials for requests to the {@link #updateURL}.
     */
    private String updatePassword;

    public SwissTimingConfigurationWithSecurityDTO() {}

    public SwissTimingConfigurationWithSecurityDTO(String name, String jsonUrl, String hostname, Integer port,
            String updateURL, String updateUsername, String updatePassword, String creatorName) {
        super();
        this.name = name;
        this.jsonUrl = jsonUrl;
        this.hostname = hostname;
        this.port = port;
        this.updateURL = updateURL;
        this.updateUsername = updateUsername;
        this.updatePassword = updatePassword;
        this.creatorName = creatorName;
    }

    public SwissTimingConfigurationWithSecurityDTO(SwissTimingConfigurationWithSecurityDTO dto, String hostname,
            Integer port, String eventName) {
        this.name = eventName;
        this.jsonUrl = dto.getJsonUrl();
        this.hostname = hostname;
        this.port = port;
        this.updateURL = dto.getUpdateURL();
        this.updateUsername = dto.getUpdateUsername();
        this.updatePassword = dto.getUpdatePassword();
        this.creatorName = dto.getCreatorName();
    }

    public String getName() {
        return name;
    }

    public String getJsonUrl() {
        return jsonUrl;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

    public String getUpdateURL() {
        return updateURL;
    }

    public String getUpdateUsername() {
        return updateUsername;
    }

    public String getUpdatePassword() {
        return updatePassword;
    }

    public String getCreatorName() {
        return creatorName;
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

    @Override
    public HasPermissions getType() {
        return SecuredDomainType.SWISS_TIMING_ACCOUNT;
    }

    private TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return creatorName == null ? new TypeRelativeObjectIdentifier(jsonUrl)
                : new TypeRelativeObjectIdentifier(jsonUrl, creatorName);
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }
}
