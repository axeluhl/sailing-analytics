package com.sap.sailing.domain.abstractlog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Apply one kind of analyzer to multiple logs, and combine (reduce) their output into one result value.
 */
public class MultiLogAnalyzer<InterimResultT, FinalResultT>
        implements LogAnalyzer<FinalResultT> {
    private final List<AbstractLog<?, ?>> logs;
    private final AnalyzerFactory<InterimResultT> analyzerFactory;
    private final ResultReducer<InterimResultT, FinalResultT> resultReducer;

    public interface AnalyzerFactory<InterimResultT> {
        LogAnalyzer<InterimResultT> createAnalyzer(AbstractLog<?, ?> log);
    }

    public interface ResultReducer<InterimResultT, FinalResultT> {
        FinalResultT getInitialFinalResultValue();
        FinalResultT reduce(InterimResultT interimResult, FinalResultT reducedFinalResult);
    }
    
    public static abstract class CollectionReducer<T, C extends Collection<T>> implements ResultReducer<Collection<T>, C> {
        @Override
        public C reduce(Collection<T> interimResult, C reducedFinalResult) {
            reducedFinalResult.addAll(interimResult);
            return reducedFinalResult;
        }
    }
    
    public static class SetReducer<T> extends CollectionReducer<T, Set<T>> {
        @Override
        public Set<T> getInitialFinalResultValue() {
            return new HashSet<T>();
        }
    }
    
    public static class ListReducer<T> extends CollectionReducer<T, List<T>> {
        @Override
        public List<T> getInitialFinalResultValue() {
            return new ArrayList<T>();
        }
    }
    
    public static class MapWithValueCollectionReducer<K, V, C extends Collection<V>>
        implements ResultReducer<Map<K, C>, Map<K, C>> {
        @Override
        public Map<K, C> getInitialFinalResultValue() {
            return new HashMap<K, C>();
        }

        @Override
        public Map<K, C> reduce(Map<K, C> interimResult, Map<K, C> reducedFinalResult) {
            for (Entry<K, C> e : interimResult.entrySet()) {
                C existing = reducedFinalResult.putIfAbsent(e.getKey(), e.getValue());
                if (existing != null) {
                    existing.addAll(e.getValue());
                }
            }
            return reducedFinalResult;
        }

    }

    public MultiLogAnalyzer(AnalyzerFactory< InterimResultT> analyzerFactory,
            ResultReducer<InterimResultT, FinalResultT> resultReducer, AbstractLog<?, ?>... logs) {
        this(analyzerFactory, resultReducer, Arrays.asList(logs));
    }
    
    public MultiLogAnalyzer(AnalyzerFactory<InterimResultT> analyzerFactory,
            ResultReducer<InterimResultT, FinalResultT> resultReducer, List<AbstractLog<?, ?>> logs) {
        this.logs = new ArrayList<AbstractLog<?, ?>>(logs);
        this.analyzerFactory = analyzerFactory;
        this.resultReducer = resultReducer;
    }

    @Override
    public FinalResultT analyze() {
        FinalResultT finalResult = resultReducer.getInitialFinalResultValue();
        for (AbstractLog<?, ?> log : logs) {
            if (log == null) {
                continue;
            }
            InterimResultT interimResult = analyzerFactory.createAnalyzer(log).analyze();
            finalResult = resultReducer.reduce(interimResult, finalResult);
        }
        return finalResult;
    }
}
