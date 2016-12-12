package com.sap.sse.gwt.client.shared.perspective;

/**
 * Helper class to leverage two parallel server calls and provide the results when both call results have been arrived.
 * 
 * @author Vladislav Chumak
 *
 */
public class CallbacksJoinerHelper<F, S> {
    
    private boolean firstCallbackReceived = false;
    private boolean secondCallbackReceived = false;
    private F firstCallbackResult;
    private S secondCallbackResult;
    private Throwable caught;
    
    public F getFirstCallbackResult() {
        return firstCallbackResult;
    }
    public void receiveFirstCallbackResult(F firstCallbackResult) {
        this.firstCallbackResult = firstCallbackResult;
        this.firstCallbackReceived = true;
    }
    public S getSecondCallbackResult() {
        return secondCallbackResult;
    }
    public void receiveSecondCallbackResult(S secondCallbackResult) {
        this.secondCallbackResult = secondCallbackResult;
        this.secondCallbackReceived = true;
    }
    public boolean hasAllCallbacksReceived() {
        return firstCallbackReceived && secondCallbackReceived;
    }
    public void receiveError(Throwable caught) {
        this.caught = caught;
    }
    public boolean isErrorOccurred() {
        return caught != null;
    }
    public Throwable getCaught() {
        return caught;
    }
    
}
