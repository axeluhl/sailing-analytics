package com.sap.sailing.grib.impl;

import java.util.Optional;

import ucar.nc2.Attribute;
import ucar.nc2.VariableSimpleIF;

public class Grib1VariableSpecification implements VariableSpecification {
    private final int indicatorOfParameter;
    
    public Grib1VariableSpecification(int indicatorOfParameter) {
        super();
        this.indicatorOfParameter = indicatorOfParameter;
    }

    @Override
    public boolean matches(VariableSimpleIF variable) {
        Optional<Integer> variableId = getGrib1VariableId(variable);
        return variableId.isPresent() && variableId.get() == indicatorOfParameter;
    }

    /**
     * Obtains the {@code Grib1_Parameter} value which gives the ID of the variable, such as 33 for the
     * "u-component of wind" variable
     * 
     * @return an optional {@link Integer} that, if present, represents the {@code Grib1_Parameter} value for the
     *         variable
     */
    private static Optional<Integer> getGrib1VariableId(VariableSimpleIF variable) {
        final Optional<Integer> result;
        final Attribute idVariable = variable.findAttributeIgnoreCase("Grib1_Parameter");
        if (idVariable != null) {
            result = Optional.of(idVariable.getNumericValue().intValue());
        } else {
            result = Optional.empty();
        }
        return result;
    }

    @Override
    public String toString() {
        return "Grib1VariableSpecification [indicatorOfParameter=" + indicatorOfParameter + "]";
    }
}
