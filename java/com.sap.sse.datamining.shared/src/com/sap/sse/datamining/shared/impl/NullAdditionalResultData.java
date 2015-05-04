package com.sap.sse.datamining.shared.impl;

import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.data.Unit;

/**
 * Null Object pattern for {@link AdditionalResultData} with the values:
 * <ul>
 *   <li><b>Retrieved Data Amount</b>: <code>0</code></li>
 *   <li><b>Calculation Time</b>: <code>0</code></li>
 *   <li><b>Result Signifier</b>: <code>""</code></li>
 *   <li><b>Unit</b>: {@link Unit#None}</li>
 *   <li><b>Unit Signifier</b>: <code>""</code></li>
 *   <li><b>Value Decimals</b>: <code>0</code></li>
 * </ul>
 * 
 * @author Lennart Hensler (D054527)
 */
public class NullAdditionalResultData implements AdditionalResultData {
    private static final long serialVersionUID = -8129840449690994767L;

    @Override
    public int getRetrievedDataAmount() {
        return 0;
    }

    @Override
    public double getCalculationTimeInSeconds() {
        return 0;
    }

    @Override
    public String getResultSignifier() {
        return "";
    }

    @Override
    public Unit getUnit() {
        return Unit.None;
    }
    
    @Override
    public String getUnitSignifier() {
        return "";
    }

    @Override
    public int getValueDecimals() {
        return 0;
    }

}
