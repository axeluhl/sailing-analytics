package com.sap.sailing.gwt.ui.shared.dispatch;

public class BatchAction implements Action<BatchResult> {
    private Action<?>[] actions;

    @SuppressWarnings("unused")
    private BatchAction() {
    }

    public BatchAction(Action<?>... actions) {
        this.actions = actions;
    }

    public Action<?>[] getActions() {
        return actions;
    }
}
