package com.sap.sailing.domain.swisstimingadapter.security;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.swisstimingadapter.HasJsonUrl;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sse.common.Named;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public class SwissTimingSecuredDomainTypes extends SecuredDomainType {

    private static final long serialVersionUID = -5481844780135214330L;

    public SwissTimingSecuredDomainTypes(String logicalTypeName, IdentifierStrategy identiferStrategy,
            Action... availableActions) {
        super(logicalTypeName, identiferStrategy, availableActions);
    }

    public SwissTimingSecuredDomainTypes(String logicalTypeName, IdentifierStrategy identiferStrategy) {
        super(logicalTypeName, identiferStrategy);
    }

    public static final HasPermissions SWISS_TIMING_ACCOUNT = new SecuredDomainType("SWISS_TIMING_ACCOUNT",
            SwissTimingsIdentifierStrategy.JSON_URL);

    /**
     * The type relative identifier is {@link SwissTimingArchiveConfiguration.getJsonUrl} which is also returned by
     * {@link SwissTimingArchiveConfiguration.getName()} of the interface {@link Named}.
     */
    public static final HasPermissions SWISS_TIMING_ARCHIVE_ACCOUNT = new SecuredDomainType(
            "SWISS_TIMING_ARCHIVE_ACCOUNT", SwissTimingsIdentifierStrategy.JSON_URL);

    private interface SwissTimingsIdentifierStrategy {
        static IdentifierStrategy JSON_URL = new IdentifierStrategy() {

            @Override
            public String getIdentifierAsString(Object... object) {
                HasJsonUrl hasJsonUrl = (HasJsonUrl) object[0];
                return WildcardPermissionEncoder.encode(hasJsonUrl.getJsonURL());
            }

        };
    }

}
