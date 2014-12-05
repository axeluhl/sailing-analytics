package com.sap.sailing.domain.abstractlog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
