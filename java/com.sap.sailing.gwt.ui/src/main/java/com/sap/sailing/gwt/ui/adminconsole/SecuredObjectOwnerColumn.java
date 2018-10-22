package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sse.common.Named;
import com.sap.sse.security.shared.NamedSecuredObjectDTO;
import com.sap.sse.security.shared.Ownership;

/**
 * {@link TextColumn Text column} extension which can be used to show {@link Ownership ownership} information of
 * {@link NamedSecuredObjectDTO secured object} table entries, if any. This column also {@link #getComparator()
 * provides} a suitable {@link Comparator comparator} which can be used for sorting purposes, if required.
 * 
 * @param table
 *            {@link AbstractCellTable table} to add the column to
 * @param columnSortHandler
 *            {@link ListHandler handler} to register the column's sort {@link Comparator comparator}
 */
/**
 * @author bbarth
 *
 * @param <T>
 */
public class SecuredObjectOwnerColumn<T extends NamedSecuredObjectDTO> extends TextColumn<T> {

    private final Function<T, Named> ownerResolver;

    /**
     * Creates a new {@link SecuredObjectOwnerColumn} instance used the provided {@link Function resolver} to determine
     * the {@link Named owner reference} who's name will be shown in this column.
     * 
     * @param ownerResolver
     *            {@link Function} to resolve the owner reference
     */
    public SecuredObjectOwnerColumn(final Function<Ownership, Named> ownerResolver) {
        final Function<T, Ownership> ownershipResolver = NamedSecuredObjectDTO::getOwnership;
        this.ownerResolver = ownershipResolver.andThen(ownerResolver);
    }

    @Override
    public final String getValue(final T object) {
        return Optional.ofNullable(ownerResolver.apply(object)).map(Named::getName).orElse("");
    }

    /**
     * @return a suitable {@link Comparator comparator} for this column
     */
    public Comparator<T> getComparator() {
        return Comparator.comparing(this::getValue);
    }

    /**
     * @return {@link SecuredObjectOwnerColumn} instance showing the {@link Ownership#getTenantOwner() tenant owner}
     */
    public static <T extends NamedSecuredObjectDTO> SecuredObjectOwnerColumn<T> getGroupOwnerColumn() {
        return new SecuredObjectOwnerColumn<>(Ownership::getTenantOwner);
    }

    /**
     * @return {@link SecuredObjectOwnerColumn} instance showing the {@link Ownership#getUserOwner() user owner}
     */
    public static <T extends NamedSecuredObjectDTO> SecuredObjectOwnerColumn<T> getUserOwnerColumn() {
        return new SecuredObjectOwnerColumn<>(Ownership::getUserOwner);
    }

}
