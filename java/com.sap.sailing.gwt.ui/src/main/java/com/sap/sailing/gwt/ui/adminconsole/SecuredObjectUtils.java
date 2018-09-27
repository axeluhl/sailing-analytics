package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.domain.common.dto.NamedSecuredObjectDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Named;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class SecuredObjectUtils {

    public static final StringMessages SECURITY_MESSAGES = GWT.create(StringMessages.class);

    public static String getTypeRelativeObjectIdentifier(final EventDTO securedObject) {
        return securedObject.id.toString();
    }

    public static String getTypeRelativeObjectIdentifier(final RegattaDTO securedObject) {
        return securedObject.getName();
    }

    public static String getTypeRelativeObjectIdentifier(final LeaderboardGroupDTO securedObject) {
        return securedObject.getId().toString();
    }
    
    public static String getTypeRelativeObjectIdentifier(final StrippedLeaderboardDTO securedObject) {
        return securedObject.getName();
    }

    /**
     * Adds sort-able {@link Column column}s to the provided {@link AbstractCellTable cell table} showing group owner
     * and user owner information of secured object table entries, if any. The {@link Comparator comparator}s used for
     * sorting will be set on the provided {@link ListHandler list handler}.
     * 
     * @param table
     *            {@link AbstractCellTable table} to add the column to
     * @param columnSortHandler
     *            {@link ListHandler handler} to register the column's sort {@link Comparator comparator}
     */
    public static <T extends NamedSecuredObjectDTO> void configureOwnerColumns(final AbstractCellTable<T> table,
            final ListHandler<T> columnSortHandler) {
        final OwnerColumn<T> groupColumn = new OwnerColumn<>(Ownership::getTenantOwner);
        table.addColumn(groupColumn, SECURITY_MESSAGES.group());
        columnSortHandler.setComparator(groupColumn, groupColumn.getComparator());
        final OwnerColumn<T> userColumn = new OwnerColumn<>(Ownership::getUserOwner);
        table.addColumn(userColumn, SECURITY_MESSAGES.user());
        columnSortHandler.setComparator(userColumn, userColumn.getComparator());
    }

    private static class OwnerColumn<T extends NamedSecuredObjectDTO> extends TextColumn<T> {

        private final Function<T, String> ownerNameResolver;

        private OwnerColumn(Function<Ownership, Named> ownerResolver) {
            final Function<T, Ownership> ownershipResolver = NamedSecuredObjectDTO::getOwnership;
            this.ownerNameResolver = ownershipResolver.andThen(ownerResolver).andThen(Named::getName);
        }

        @Override
        public final String getValue(T object) {
            return Optional.ofNullable(ownerNameResolver.apply(object)).orElse("");
        }

        private Comparator<T> getComparator() {
            return Comparator.comparing(this::getValue);
        }

    }
}
