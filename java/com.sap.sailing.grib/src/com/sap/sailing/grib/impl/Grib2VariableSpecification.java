package com.sap.sailing.grib.impl;

import java.util.Arrays;
import java.util.Optional;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.VariableSimpleIF;

public class Grib2VariableSpecification implements VariableSpecification {
    private final int[] disciplineCategoryNumberAndMoreOptionalSpecifiers;
    
    public Grib2VariableSpecification(int[] disciplineCategoryNumberAndMoreOptionalSpecifiers) {
        super();
        this.disciplineCategoryNumberAndMoreOptionalSpecifiers = disciplineCategoryNumberAndMoreOptionalSpecifiers;
    }

    @Override
    public boolean matches(VariableSimpleIF variable) {
        final boolean result;
        final Optional<int[]> grib2ParameterInfo = getGrib2VariableId(variable);
        if (grib2ParameterInfo.isPresent() && grib2ParameterInfo.get().length >= disciplineCategoryNumberAndMoreOptionalSpecifiers.length) {
            result = Arrays.equals(Arrays.copyOf(grib2ParameterInfo.get(), disciplineCategoryNumberAndMoreOptionalSpecifiers.length), disciplineCategoryNumberAndMoreOptionalSpecifiers);
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Obtains the {@code Grib2_Parameter} value which gives the ID of the variable, such as "VAR_0-2-1_L103" for the
     * "WIND" wind speed variable
     * 
     * @return an optional {@link Integer} that, if present, represents the {@code Grib1_Parameter} value for the
     *         variable
     */
    private static Optional<int[]> getGrib2VariableId(VariableSimpleIF variable) {
        final Optional<int[]> result;
        final Attribute idVariable = variable.findAttributeIgnoreCase("Grib2_Parameter");
        if (idVariable != null) {
            final Array grib2ParameterDisciplineCategoryAndNumber = idVariable.getValues();
            if (grib2ParameterDisciplineCategoryAndNumber == null) {
                result = Optional.empty();
            } else {
                final int[] resultArray = new int[(int) grib2ParameterDisciplineCategoryAndNumber.getSize()];
                for (int i=0; i<resultArray.length; i++) {
                    resultArray[i] = grib2ParameterDisciplineCategoryAndNumber.getInt(i);
                }
                result = Optional.of(resultArray);
            }
        } else {
            result = Optional.empty();
        }
        return result;
    }

    @Override
    public String toString() {
        return "Grib2VariableSpecification [disciplineCategoryNumberAndMoreOptionalSpecifiers="
                + Arrays.toString(disciplineCategoryNumberAndMoreOptionalSpecifiers) + "]";
    }
    
}
