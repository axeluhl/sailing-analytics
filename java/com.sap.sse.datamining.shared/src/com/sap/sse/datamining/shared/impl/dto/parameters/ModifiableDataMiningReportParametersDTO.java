package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.datamining.shared.dto.DataMiningReportParametersDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;

public class ModifiableDataMiningReportParametersDTO implements DataMiningReportParametersDTO {
    
    private final Map<ParameterKey, FilterDimensionParameter> parameters;
    private final Map<QueryKey, Collection<ParameterKey>> usages;
    
    public ModifiableDataMiningReportParametersDTO() {
        parameters = new HashMap<>();
        usages = new HashMap<>();
    }
    
    public ModifiableDataMiningReportParametersDTO(ModifiableDataMiningReportParametersDTO source) {
        this.parameters = source.getAll();
        this.usages = new HashMap<>();
        for (Entry<QueryKey, Collection<ParameterKey>> entry : source.usages.entrySet()) {
            this.usages.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    @Override
    public Map<ParameterKey, FilterDimensionParameter> getAll() {
        return new HashMap<>(parameters);
    }

    @Override
    public boolean contains(ParameterKey key) {
        return parameters.containsKey(key);
    }

    @Override
    public FilterDimensionParameter get(ParameterKey key) {
        return parameters.get(key);
    }
    
    public void add(ParameterKey key, FilterDimensionParameter parameter) {
        parameters.put(key, parameter);
    }
    
    public void remove(ParameterKey key) {
        parameters.remove(key);
    }

}
