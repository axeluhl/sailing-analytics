package com.sap.sailing.grib.impl;

import ucar.nc2.VariableSimpleIF;
import ucar.nc2.ft.FeatureDataset;

/**
 * GRIB variables can be described by a GRIB1 "indicatorOfParameter" which is a single integer, such as {@code 31} for
 * the wind direction (see http://apps.ecmwf.int/codes/grib/param-db?id=3031), or by a GRIB2 integer sequence starting with
 * (discipline, parameterCategory, parameterNumber) such as (0, 2, 0) for the wind direction. An instance of a class
 * implementing this interface is able to tell for a given {@link VariableSimpleIF} whether this specification matches
 * the variable.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface VariableSpecification {
    /**
     * Checks whether the given {@code variable} conforms to this variable specification.
     */
    boolean matches(VariableSimpleIF variable);
    
    default boolean appearsInAnyOf(FeatureDataset... dataSets) {
        for (final FeatureDataset dataSet : dataSets) {
            for (final VariableSimpleIF variable : dataSet.getDataVariables()) {
                if (matches(variable)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Obtains the variable from the collection of {@code variables} that {@link #matches(VariableSimpleIF) matches}
     * this specification. If no such variable exists in {@code variables}, {@code null} is returned.
     */
    default VariableSimpleIF getVariable(Iterable<VariableSimpleIF> variables) {
        for (final VariableSimpleIF variable : variables) {
            if (matches(variable)) {
                return variable;
            }
        }
        return null;
    }
}
