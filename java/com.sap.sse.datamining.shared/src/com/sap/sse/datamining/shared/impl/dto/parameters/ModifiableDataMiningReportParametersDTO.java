package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.sap.sse.datamining.shared.dto.DataMiningReportParametersDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;

public class ModifiableDataMiningReportParametersDTO implements DataMiningReportParametersDTO {
    private static final long serialVersionUID = -1898369096479655505L;
    
    private HashSet<FilterDimensionParameter> parameters;
    private HashMap<Integer, HashSet<FilterDimensionParameter>> usages;
    
    public ModifiableDataMiningReportParametersDTO() {
        parameters = new HashSet<>();
        usages = new HashMap<>();
    }
    
    public ModifiableDataMiningReportParametersDTO(DataMiningReportParametersDTO source) {
        this.parameters = source.getAll();
        this.usages = source.getAllUsages();
    }
    
    @Override
    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    @Override
    public HashSet<FilterDimensionParameter> getAll() {
        return new HashSet<>(parameters);
    }

    @Override
    public boolean contains(FilterDimensionParameter parameter) {
        return parameters.contains(parameter);
    }
    
    public boolean add(FilterDimensionParameter parameter) {
        return parameters.add(parameter);
    }
    
    public boolean remove(FilterDimensionParameter parameter) {
        boolean removed = parameters.remove(parameter);
        if (removed) {
            Collection<Integer> keysToRemove = new ArrayList<>();
            usages.forEach((key, usages) -> {
                usages.remove(parameter);
                if (usages.isEmpty()) {
                    keysToRemove.add(key);
                }
            });
            keysToRemove.forEach(usages::remove); // TODO this?
        }
        return removed;
    }
    
    @Override
    public HashMap<Integer, HashSet<FilterDimensionParameter>> getAllUsages() {
        return createUsagesCopy(usages);
    }

    @Override
    public HashSet<FilterDimensionParameter> getUsages(Integer key) {
        if (usages.containsKey(key)) {            
            return new HashSet<>(usages.get(key));
        } else {
            return new HashSet<>();
        }
    }
    
    public boolean addUsage(Integer key, FilterDimensionParameter parameter) {
        if (!parameters.contains(parameter)) {
            throw new IllegalArgumentException("Cannot add usage of parameter that is not conained");
        }
        HashSet<FilterDimensionParameter> usages = this.usages.get(key);
        if (usages == null) {
            usages = new HashSet<>();
            this.usages.put(key, usages);
        }
        return usages.add(parameter);
    }
    
    public void removeUsagesAndShiftKeys(Integer key) {
        this.usages.remove(key);
        this.usages.keySet().stream().filter(k -> k > key).sorted().forEach(k -> {
            this.usages.put(k - 1, this.usages.get(k));
            this.usages.remove(k);
        });
    }
    
    @Override
    public boolean hasUsages() {
        return usages.values().stream().flatMap(c -> c.stream()).anyMatch(parameters::contains);
    }
    
    @Override
    public boolean hasUsages(Integer key) {
        return usages.containsKey(key);
    }
    
    @Override
    public boolean isUsed(FilterDimensionParameter parameter) {
        return usages.values().stream().anyMatch(c -> c.contains(parameter));
    }
    
    private HashMap<Integer, HashSet<FilterDimensionParameter>> createUsagesCopy(HashMap<Integer, HashSet<FilterDimensionParameter>> usages) {
        HashMap<Integer, HashSet<FilterDimensionParameter>> copy = new HashMap<>();
        for (Entry<Integer, HashSet<FilterDimensionParameter>> entry : usages.entrySet()) {
            this.usages.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }
}
