package com.sap.sse.gwt.dispatch.client.system.batching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sse.gwt.dispatch.client.commands.Result;
import com.sap.sse.gwt.dispatch.client.exceptions.DispatchException;

public final class BatchResult implements Result {
    private ArrayList<Result> results = new ArrayList<>();
    private ArrayList<DispatchException> exceptions = new ArrayList<>();

    protected BatchResult() {
    }

    @GwtIncompatible
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

    /**
     * Retrieves the result at a given position only if it has the right type. Type safety is ensured by comparing class
     * name. We cannot use isAssignableFrom because this is GWT/ client code.
     * 
     * @param i
     *            result position
     * @param type
     *            required result type
     * @return result at given position
     */
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
