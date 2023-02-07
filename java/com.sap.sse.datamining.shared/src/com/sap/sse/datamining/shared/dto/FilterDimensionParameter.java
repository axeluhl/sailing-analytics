package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;

import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.common.Util;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

/**
 * A parameter that can be used to keep multiple dimension filters "in sync" so that updates to one of the dimension
 * filters bound to this parameter will affect all other dimension filters bound to this same parameter simultaneously.
 * <p>
 * 
 * An instance of this type combines the "formal" parameter aspects as well as the "actual" parameter value in one
 * objects. The "formal" or "defining" part is currently made up of the {@link #getRetrieverLevel() retriever level} and
 * the {@link #getDimension() dimension function} where the dimension function is expected to be reached starting from
 * the {@link DataRetrieverLevelDTO#getRetrievedDataType()} by referring to a dimension defined directly on that type,
 * or indirectly through one or more {@code @Connector} links. Future extensions may make this less ambiguous by also
 * considering such {@code @Connector} paths as part of the key identifying the "formal" defining part of this
 * parameter.
 * <p>
 * 
 * The actual parameter value is provided by {@link #getValues}. This currently assumes that a parameter always is an
 * explicit value list, and binding the parameter to a dimension filter means that the {@link #getValues() value set} is
 * intersected with the dimension filter's set of available values (possibly already reduced by preceding dimension
 * filters) and all remaining values are then selected in the dimension filter. Conversely, changing the selected set of
 * a dimension filter that is bound to this parameter will update this parameter's {@link #getValues() values} and
 * then transitively the selected value set of all other dimension filters also bound to this parameter.
 */
public interface FilterDimensionParameter extends NamedWithUUID {
    /**
     * Must match with the {@link FunctionDTO#getReturnTypeName()} of the dimension {@link FunctionDTO function} of
     * the dimension filters to which this parameter is bound.<p>
     * 
     * In theory, type <em>conformance</em> would be sufficient, but as we're operating on type <em>names</em> only,
     * we can only test for <em>equivalence</em>.
     */
    String getTypeName();
    
    /**
     * @return a non-live snapshot copy of the actual values currently set for this parameter; these values are to be
     *         applied simultaneously to all dimension filters that are bound to this parameter.
     */
    Iterable<? extends Serializable> getValues();
    
    default boolean matches(Serializable value) {
        return Util.contains(getValues(), value);
    }
}
