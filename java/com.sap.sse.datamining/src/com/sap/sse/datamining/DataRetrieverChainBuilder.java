package com.sap.sse.datamining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;

public interface DataRetrieverChainBuilder<DataSourceType> {

    public Class<?> getCurrentRetrievedDataType();

    public <T> DataRetrieverChainBuilder<DataSourceType> setFilter(FilterCriterion<T> filter);
    public <T> DataRetrieverChainBuilder<DataSourceType> addResultReceiver(Processor<T, ?> resultReceiver);

    public DataRetrieverChainBuilder<DataSourceType> stepDeeper();

    public Processor<DataSourceType, ?> build();
    
    public class TypeSafeFilterCriterionCollection {
        
        private final Map<Class<?>, FilterCriterion<?>> criteriaMappedByElementType;
        
        public TypeSafeFilterCriterionCollection() {
            criteriaMappedByElementType = new HashMap<>();
        }
        
        public <T> void setCriterion(Class<T> elementType, FilterCriterion<T> criterion) {
            criteriaMappedByElementType.put(elementType, criterion);
        }
        
        @SuppressWarnings("unchecked") // The way the criteria are set, the cast has to work
        public <T> FilterCriterion<T> getCriterion(Class<T> elementType) {
            return (FilterCriterion<T>) criteriaMappedByElementType.get(elementType);
        }
        
    }
    
    public class TypeSafeResultReceiverCollection {
        
        private final Map<Class<?>, Collection<Processor<?, ?>>> receiversMappedByInputType;
        
        public TypeSafeResultReceiverCollection() {
            receiversMappedByInputType = new HashMap<>();
        }
        
        public <T> void addResultReceiver(Class<T> inputType, Processor<T, ?> resultReceiver) {
            if (!receiversMappedByInputType.containsKey(inputType)) {
                receiversMappedByInputType.put(inputType, new ArrayList<Processor<?, ?>>());
            }
            
            receiversMappedByInputType.get(inputType).add(resultReceiver);
        }

        @SuppressWarnings("unchecked") // The way the processors were added, the cast has to work
        public <T> Collection<Processor<T, ?>> getResultReceivers(Class<T> inputType) {
            return (Collection<Processor<T, ?>>)(Collection<?>) receiversMappedByInputType.get(inputType);
        }
        
    }

}
