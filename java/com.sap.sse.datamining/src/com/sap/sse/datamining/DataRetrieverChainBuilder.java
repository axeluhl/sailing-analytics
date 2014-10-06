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
    public <T> DataRetrieverChainBuilder<DataSourceType> addAllResultReceivers(Collection<Processor<T, ?>> resultReceivers);

    public boolean canStepDeeper();
    public DataRetrieverChainBuilder<DataSourceType> stepDeeper();

    public Processor<DataSourceType, ?> build();
    
    public class TypeSafeFilterCriteriaCollection {
        
        private final Map<Class<?>, FilterCriterion<?>> criteriaMappedByElementType;
        
        public TypeSafeFilterCriteriaCollection() {
            criteriaMappedByElementType = new HashMap<>();
        }
        
        public <T> void setCriterion(Class<T> elementType, FilterCriterion<T> criterion) {
            criteriaMappedByElementType.put(elementType, criterion);
        }
        
        @SuppressWarnings("unchecked") // The cast has to work, due to the way the criteria were set
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

        @SuppressWarnings("unchecked") // The cast has to work, due to the way the processors were added
        public <T> Collection<Processor<T, ?>> getResultReceivers(Class<T> inputType) {
            return (Collection<Processor<T, ?>>)(Collection<?>) receiversMappedByInputType.get(inputType);
        }
        
    }

}
