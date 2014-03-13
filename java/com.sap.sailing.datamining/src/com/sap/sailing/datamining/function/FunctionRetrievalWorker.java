package com.sap.sailing.datamining.function;

import java.lang.reflect.Method;
import java.util.Collection;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.DataRetrievalWorker;
import com.sap.sailing.datamining.WorkReceiver;
import com.sap.sailing.datamining.function.impl.FilteringFunctionRetrievalWorker;
import com.sap.sailing.datamining.function.impl.MethodIsCorrectDimensionFilterCriteria;
import com.sap.sailing.datamining.function.impl.MethodIsCorrectExternalFunctionFilterCriteria;
import com.sap.sailing.datamining.function.impl.MethodIsCorrectSideEffectFreeValueFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.OrCompoundFilterCriteria;

public interface FunctionRetrievalWorker extends DataRetrievalWorker<Iterable<Class<?>>, Function> {
    
    public static final class Util {
        
        public static FunctionRetrievalWorker createMarkedFunctionRetrievalWorker(Iterable<Class<?>> classesToScan, WorkReceiver<Collection<Function>> receiver) {
            FilteringFunctionRetrievalWorker worker = new FilteringFunctionRetrievalWorker();
            worker.setSource(classesToScan);
            worker.setReceiver(receiver);
            
            CompoundFilterCriteria<Method> filterCriteria = new OrCompoundFilterCriteria<>();
            filterCriteria.addCriteria(new MethodIsCorrectDimensionFilterCriteria());
            filterCriteria.addCriteria(new MethodIsCorrectSideEffectFreeValueFilterCriteria());
            worker.setFilter(filterCriteria);
            
            return worker;
        }

        public static FunctionRetrievalWorker createExternalFunctionRetrievalWorker(Iterable<Class<?>> classesToScan, WorkReceiver<Collection<Function>> receiver) {
            FilteringFunctionRetrievalWorker worker = new FilteringFunctionRetrievalWorker();
            worker.setSource(classesToScan);
            worker.setReceiver(receiver);
            
            ConcurrentFilterCriteria<Method> filterCriteria = new MethodIsCorrectExternalFunctionFilterCriteria();
            worker.setFilter(filterCriteria);
            
            return worker;
        }
        
    }

}
