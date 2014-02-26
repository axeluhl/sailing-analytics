package com.sap.sailing.selenium.pages.gwt.query;

import java.util.List;

/**
 * <p>Operation represents an operation with operator and arguments.</p>
 * 
 * @author D049941
 *
 * @param <T>
 *   The type the operation is bound to.
 */
public interface Operation<T> extends Expression<T> {
    /**
     * <p>Returns the arguments of the operation.</p>
     *
     * @return
     *   The arguments of the operation.
     */
    List<Expression<?>> getArguments();

    /**
     * <p>Returns the argument with the given index.</p>
     *
     * @param index
     *   The index of the argument.
     * @return
     *   The argument with the given index.
     */
    Expression<?> getArgument(int index);
}
