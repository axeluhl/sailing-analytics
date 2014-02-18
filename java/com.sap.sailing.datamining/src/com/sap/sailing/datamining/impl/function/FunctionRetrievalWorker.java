package com.sap.sailing.datamining.impl.function;

import java.lang.reflect.Method;
import java.util.Collection;

import com.sap.sailing.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.OrCompoundFilterCriteria;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.workers.DataRetrievalWorker;
import com.sap.sse.datamining.workers.WorkReceiver;

public interface FunctionRetrievalWorker extends DataRetrievalWorker<Iterable<Class<?>>, Function<?>> {
    
    public static final class Util {
        
        public static FunctionRetrievalWorker createMarkedFunctionRetrievalWorker(Iterable<Class<?>> classesToScan, WorkReceiver<Collection<Function<?>>> receiver) {
            FilteringFunctionRetrievalWorker worker = new FilteringFunctionRetrievalWorker();
            worker.setSource(classesToScan);
            worker.setReceiver(receiver);
            
            CompoundFilterCriteria<Method> filterCriteria = new OrCompoundFilterCriteria<>();
            filterCriteria.addCriteria(new MethodIsCorrectDimensionFilterCriteria());
            filterCriteria.addCriteria(new MethodIsCorrectSideEffectFreeValueFilterCriteria());
            worker.setFilter(filterCriteria);
            
            return worker;
        }

        public static FunctionRetrievalWorker createExternalFunctionRetrievalWorker(Iterable<Class<?>> classesToScan, WorkReceiver<Collection<Function<?>>> receiver) {
            FilteringFunctionRetrievalWorker worker = new FilteringFunctionRetrievalWorker();
            worker.setSource(classesToScan);
            worker.setReceiver(receiver);
            
            FilterCriteria<Method> filterCriteria = new MethodIsCorrectExternalFunctionFilterCriteria();
            worker.setFilter(filterCriteria);
            
            return worker;
        }
        
    }

}
