package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public class NumberDataProvider extends AbstractResultDataProvider<Number> {

    private final static String FLOAT = "Float";
    private final static String INTEGER = "Integer";
    private final Collection<String> dataKeys;

    public NumberDataProvider() {
        super(Number.class);
        dataKeys = new ArrayList<>();
        dataKeys.add(FLOAT);
        dataKeys.add(INTEGER);
    }

    @Override
    public Collection<String> getDataKeys() {
        return dataKeys;
    }
    
    @Override
    public boolean acceptsResultsOfType(String type) {
        return type.equals(Number.class.getName()) ||
               type.equals(Double.class.getName()) ||
               type.equals(Float.class.getName()) ||
               type.equals(Long.class.getName()) ||
               type.equals(Integer.class.getName()) ||
               type.equals(Short.class.getName()) ||
               type.equals(Byte.class.getName()) ||
               type.equals(double.class.getName()) ||
               type.equals(float.class.getName()) ||
               type.equals(long.class.getName()) ||
               type.equals(int.class.getName()) ||
               type.equals(short.class.getName()) ||
               type.equals(byte.class.getName());
    }

    @Override
    protected Number getData(Number number, String dataKey) {
        switch (dataKey) {
        case FLOAT:
            return number.doubleValue();
        case INTEGER:
            return number.intValue();
        }
        throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
    }
    
    @Override
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return result.getValueDecimals() == 0 ? INTEGER : FLOAT;
    }

}
