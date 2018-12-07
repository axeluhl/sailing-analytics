package com.sap.sailing.domain.igtimiadapter.security;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.IdentifierStrategy;

public class IgtimiSecuredDomainType extends SecuredDomainType {

    private static final long serialVersionUID = 5210955737253860470L;

    public IgtimiSecuredDomainType(String logicalTypeName, IdentifierStrategy identiferStrategy,
            Action... availableActions) {
        super(logicalTypeName, identiferStrategy, availableActions);
    }

    public IgtimiSecuredDomainType(String logicalTypeName, IdentifierStrategy identiferStrategy) {
        super(logicalTypeName, identiferStrategy);
    }

    /**
     * Describes access permissions to Igtimi account objects. Type-relative object identifier is the e-mail address
     * string representing the account.
     */
    public static final HasPermissions IGTIMI_ACCOUNT = new SecuredDomainType("IGTIMI_ACCOUNT",
            IgtimiIdentifierStrategy.IGITIMI_ACCOUNT);

    private interface IgtimiIdentifierStrategy {
        static IdentifierStrategy IGITIMI_ACCOUNT = new IdentifierStrategy() {

            @Override
            public <T> String getIdentifierAsString(T object) {
                Account account = (Account) object;
                return account.getUser().getEmail();
            }

        };
    }
}