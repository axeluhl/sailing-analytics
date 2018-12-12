package com.sap.sailing.domain.tractracadapter.security;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public class TracTracSecuredDomainType extends SecuredDomainType {

    private static final long serialVersionUID = 1051267403239212083L;

    public TracTracSecuredDomainType(String logicalTypeName, IdentifierStrategy identiferStrategy,
            Action... availableActions) {
        super(logicalTypeName, identiferStrategy, availableActions);
    }

    public TracTracSecuredDomainType(String logicalTypeName, IdentifierStrategy identiferStrategy) {
        super(logicalTypeName, identiferStrategy);
    }

    public static final HasPermissions TRACTRAC_ACCOUNT = new SecuredDomainType("TRACTRAC_ACCOUNT",
            TracTracIdentifierStrategy.TRACTRAC_ACCOUNT);

    private interface TracTracIdentifierStrategy {
        static IdentifierStrategy TRACTRAC_ACCOUNT = new IdentifierStrategy() {

            @Override
            public String getIdentifierAsString(Object... object) {
                assert object.length == 1;
                TracTracConfiguration tracTracConfiguration = (TracTracConfiguration) object[0];
                return WildcardPermissionEncoder.encode(tracTracConfiguration.getJSONURL());
            }

            @Override
            public String convertNewIdentifer(Object... object) {
                // TODO: check again, what type can be used, for now assuming String for the
                // tracTracConfiguration.getJSONURL()
                assert object.length == 1;
                String identifier = (String) object[0];
                return WildcardPermissionEncoder.encode(identifier);
            }

        };
    }
}
