package com.sap.sailing.domain.abstractlog;


/**
 * Analyzer to perform a query over a given AbstractLog. Each subclass defines its <code>ResultType</code>.
 * 
 * @param <ResultT>
 *            type of analysis result.
 */
public abstract class BaseLogAnalyzer<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT, ResultT>
        implements LogAnalyzer<ResultT> {

    protected LogT log;

    public BaseLogAnalyzer(LogT log) {
        this.log = log;
    }

    public LogT getLog() {
        return log;
    }

    @Override
    public ResultT analyze() {
        log.lockForRead();
        try {
            return performAnalysis();
        } finally {
            log.unlockAfterRead();
        }
    }

    protected abstract ResultT performAnalysis();

    protected Iterable<EventT> getAllEvents() {
        return log.getRawFixes();
    }

    protected Iterable<EventT> getAllEventsDescending() {
        return log.getRawFixesDescending();
    }
}
