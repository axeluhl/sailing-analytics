package com.sap.sailing.expeditionconnector.security;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;
import com.sap.sse.ServerInfo;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public class ExpeditionSecuredDomainTypes extends SecuredDomainType {

    private static final long serialVersionUID = 8407831418440433245L;

    public ExpeditionSecuredDomainTypes(String logicalTypeName, IdentifierStrategy identiferStrategy,
            Action... availableActions) {
        super(logicalTypeName, identiferStrategy, availableActions);
    }

    public ExpeditionSecuredDomainTypes(String logicalTypeName, IdentifierStrategy identiferStrategy) {
        super(logicalTypeName, identiferStrategy);
    }

    /**
     * Describes access permissions to {@code ExpeditionDeviceConfiguration} objects. Type-relative object identifier is
     * the WildcardPermissionEncoder.encode(getServerInfo().getServerName(), deviceConfiguration.getName());
     */
    public static final HasPermissions EXPEDITION_DEVICE_CONFIGURATION = new SecuredDomainType(
            "EXPEDITION_DEVICE_CONFIGURATION", SwissTimingsIdentifierStrategy.EXPEDITION_DEVICE_CONFIGURATION);

    private interface SwissTimingsIdentifierStrategy {
        static IdentifierStrategy EXPEDITION_DEVICE_CONFIGURATION = new IdentifierStrategy() {

            @Override
            public String getIdentifierAsString(Object... object) {
                assert object.length == 1;
                ExpeditionDeviceConfiguration expeditionDeviceConfiguration = (ExpeditionDeviceConfiguration) object[0];
                return WildcardPermissionEncoder.encode(ServerInfo.getName(), expeditionDeviceConfiguration.getName());
            }

        };
    }
}
