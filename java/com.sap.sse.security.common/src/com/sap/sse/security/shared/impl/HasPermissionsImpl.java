package com.sap.sse.security.shared.impl;

import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;

public class HasPermissionsImpl extends NamedImpl implements HasPermissions {
    private static final long serialVersionUID = -7901836864741040400L;
    private final Action[] availableActions;

    /**
     * By default, all actions as provided by {@link DefaultActions} are supported by logical types with permissions
     * constructed with this constructor.
     * 
     * @param logicalTypeName
     *            a type name that can be represented in a {@link WildcardPermission}'s first part without further need
     *            for encoding
     */
    public HasPermissionsImpl(String logicalTypeName) {
        this(logicalTypeName, DefaultActions.values());
    }

    /**
     * No default actions from {@link DefaultActions} are added implicitly to the set of actions available for the
     * logical secured type constructed here. All actions available need to be passed explicitly as part of the
     * {@code availableActions} parameter.
     * 
     * @param logicalTypeName
     *            a type name that can be represented in a {@link WildcardPermission}'s first part without further need
     *            for encoding
     */
    public HasPermissionsImpl(final String logicalTypeName, final Action... availableActions) {
        super(logicalTypeName);
        assert logicalTypeName.equals(new WildcardPermissionEncoder().encodeAsPermissionPart(logicalTypeName));
        final int numberOfActionsAvailable = availableActions == null ? 0 : availableActions.length;
        this.availableActions = new Action[numberOfActionsAvailable];
        if (availableActions != null) {
            System.arraycopy(availableActions, 0, this.availableActions, 0, numberOfActionsAvailable);
        }
    }

    @Override
    public Action[] getAvailableActions() {
        return availableActions;
    }

    @Override
    public boolean supports(final Action action) {
        for (final Action supportedAction : availableActions) {
            if (action.equals(supportedAction)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getStringPermission(final Action... actions) {
        final String result;
        if (actions == null || actions.length == 0) {
            result = getName();
        } else {
            final StringBuilder modesString = new StringBuilder();
            boolean first = true;
            for (final Action action : actions) {
                assert supports(action);
                if (first) {
                    first = false;
                } else {
                    modesString.append(',');
                }
                modesString.append(action.name());
            }
            result = getName() + ":" + modesString.toString();
        }
        return result;
    }

    @Override
    public WildcardPermission getPermission(final Action... actions) {
        return new WildcardPermission(getStringPermission(actions));
    }

    @Override
    public String getStringPermissionForObjects(final Action action, final String... typeRelativeObjectIdentifiers) {
        assert supports(action);
        final WildcardPermissionEncoder permissionEncoder = new WildcardPermissionEncoder();
        final StringBuilder result = new StringBuilder(getStringPermission(action));
        if (typeRelativeObjectIdentifiers != null && typeRelativeObjectIdentifiers.length > 0) {
            result.append(':');
            boolean first = true;
            for (String typeRelativeObjectIdentifier : typeRelativeObjectIdentifiers) {
                if (first) {
                    first = false;
                } else {
                    result.append(',');
                }
                result.append(permissionEncoder.encodeAsPermissionPart(typeRelativeObjectIdentifier));
            }
        }
        return result.toString();
    }

    @Override
    public QualifiedObjectIdentifier getQualifiedObjectIdentifier(final String typeRelativeObjectIdentifier) {
        return new QualifiedObjectIdentifierImpl(getName(), typeRelativeObjectIdentifier);
    }

    @Override
    public WildcardPermission getPermissionForObjects(final Action action, final String... objectIdentifiers) {
        assert supports(action);
        return new WildcardPermission(getStringPermissionForObjects(action, objectIdentifiers));
    }
}
