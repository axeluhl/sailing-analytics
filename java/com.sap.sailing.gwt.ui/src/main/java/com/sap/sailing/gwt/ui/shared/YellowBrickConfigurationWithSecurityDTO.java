package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickConfiguration;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.dto.NamedSecuredObjectDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class YellowBrickConfigurationWithSecurityDTO extends NamedSecuredObjectDTO {
    private static final long serialVersionUID = -3567107321280535272L;

    private String name;
    private String raceUrl;
    private String username;
    private String password;
    private String creatorName;

    @Deprecated // GWT only
    YellowBrickConfigurationWithSecurityDTO() {
    }

    public YellowBrickConfigurationWithSecurityDTO(SecurityInformationDTO securityInformation, String name,
            String raceUrl, String username, String password, String creatorName) {
        super(name, securityInformation);
        this.raceUrl = raceUrl;
        this.username = username;
        this.password = password;
        this.creatorName = creatorName;
    }

    /** Copy constructor with new name */
    public YellowBrickConfigurationWithSecurityDTO(YellowBrickConfigurationWithSecurityDTO config, final String name) {
        this(config.getSecurityInformation(), name, config.getRaceUrl(), config.getUsername(), config.getPassword(), config.getCreatorName());
    }

    public String getName() {
        return name;
    }

    public String getRaceUrl() {
        return raceUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCreatorName() {
        return creatorName;
    }

    @Override
    public HasPermissions getPermissionType() {
        return SecuredDomainType.YELLOWBRICK_ACCOUNT;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(
                YellowBrickConfiguration.getTypeRelativeObjectIdentifier(getRaceUrl(), getCreatorName()));
    }
}
