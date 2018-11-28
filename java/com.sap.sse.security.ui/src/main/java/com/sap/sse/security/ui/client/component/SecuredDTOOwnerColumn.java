package com.sap.sse.security.ui.client.component;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sse.common.Named;
import com.sap.sse.security.shared.SecuredDTO;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.OwnershipDTO;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * {@link TextColumn Text column} extension which can be used to show {@link Ownership ownership} information of
 * {@link SecuredObject secured object} table entries, if any. This column also {@link #getComparator() provides} a
 * suitable {@link Comparator comparator} which can be used for sorting purposes, if required.
 * 
 * @param table
 *            {@link AbstractCellTable table} to add the column to
 * @param columnSortHandler
 *            {@link ListHandler handler} to register the column's sort {@link Comparator comparator}
 *
 * @param <T>
 *            the actual {@link SecuredObject} sub-type
 */
public class SecuredDTOOwnerColumn<T extends SecuredDTO> extends TextColumn<T> {

    private final Function<T, Optional<Named>> ownerResolver;

    /**
     * Creates a new {@link SecuredDTOOwnerColumn} instance used the provided {@link Function resolver} to determine
     * the {@link Named owner reference} who's name will be shown in this column.
     * 
     * @param ownerResolver
     *            {@link Function} to resolve the owner reference
     */
    public SecuredDTOOwnerColumn(final Function<OwnershipDTO, Named> ownerResolver) {
        final Function<T, OwnershipDTO> ownershipResolver = SecuredDTO::getOwnership;
        this.ownerResolver = ownershipResolver.andThen(ownership -> Optional.ofNullable(ownership).map(ownerResolver));
    }

    @Override
    public final String getValue(final T object) {
        return ownerResolver.apply(object).map(Named::getName).orElse("");
    }

    /**
     * @return a suitable {@link Comparator comparator} for this column
     */
    public Comparator<T> getComparator() {
        return Comparator.comparing(this::getValue);
    }

    /**
     * @return {@link SecuredDTOOwnerColumn} instance showing the {@link Ownership#getTenantOwner() tenant owner}
     */
    public static <T extends SecuredDTO> SecuredDTOOwnerColumn<T> getGroupOwnerColumn() {
        return new SecuredDTOOwnerColumn<>(OwnershipDTO::getTenantOwner);
    }

    /**
     * @return {@link SecuredDTOOwnerColumn} instance showing the {@link Ownership#getUserOwner() user owner}
     */
    public static <T extends SecuredDTO> SecuredDTOOwnerColumn<T> getUserOwnerColumn() {
        return new SecuredDTOOwnerColumn<>(OwnershipDTO::getUserOwner);
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
     * @param stringMessages
     *            {@link StringMessages} to use for the column header texts
     */
    public static <T extends SecuredDTO> void configureOwnerColumns(final AbstractCellTable<T> table,
            final ListHandler<T> columnSortHandler, final StringMessages stringMessages) {
        final SecuredDTOOwnerColumn<T> groupColumn = getGroupOwnerColumn();
        table.addColumn(groupColumn, stringMessages.group());
        columnSortHandler.setComparator(groupColumn, groupColumn.getComparator());
        final SecuredDTOOwnerColumn<T> userColumn = getUserOwnerColumn();
        table.addColumn(userColumn, stringMessages.user());
        columnSortHandler.setComparator(userColumn, userColumn.getComparator());
    }

}
