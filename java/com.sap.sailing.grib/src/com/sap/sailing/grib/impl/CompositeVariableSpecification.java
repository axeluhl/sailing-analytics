package com.sap.sailing.grib.impl;

import java.util.Arrays;

import ucar.nc2.VariableSimpleIF;

public class CompositeVariableSpecification implements VariableSpecification {
    private final VariableSpecification[] variableSpecifications;
    
    public CompositeVariableSpecification(VariableSpecification... variableSpecifications) {
        super();
        this.variableSpecifications = variableSpecifications;
    }

    @Override
    public boolean matches(VariableSimpleIF variable) {
        for (final VariableSpecification s : variableSpecifications) {
            if (s.matches(variable)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "CompositeVariableSpecification [variableSpecifications=" + Arrays.toString(variableSpecifications)
                + "]";
    }
}
