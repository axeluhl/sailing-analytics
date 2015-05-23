package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BatchResult implements Result {
    private LinkedList<Result> results = new LinkedList<Result>();

    private LinkedList<DispatchException> exceptions = new LinkedList<DispatchException>();

    @SuppressWarnings("unused")
    private BatchResult() {
    }

    public BatchResult(List<Result> results, List<DispatchException> exceptions) {
        this.results.addAll(results);
        this.exceptions.addAll(exceptions);
    }

    public List<Result> getResults() {
        return results;
    }

    public List<DispatchException> getExceptions() {
        return exceptions;
    }

    public int size() {
        return results.size();
    }

    public Result getResult(int i) {
        return results.get(i);
    }

    @SuppressWarnings("unchecked")
    public <T extends Result> T getResult(int i, Class<T> type) {
        Object result = results.get(i);
        if (result != null && type.getName().equals(result.getClass().getName()))
            return (T) result;
        return null;
    }

    public Throwable getException(int index) {
        if (index < exceptions.size())
            return exceptions.get(index);
        else
            return null;
    }

    public Iterator<Result> iterator() {
        return results.iterator();
    }
}
