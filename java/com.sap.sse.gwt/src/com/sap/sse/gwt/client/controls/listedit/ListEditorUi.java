package com.sap.sse.gwt.client.controls.listedit;

import com.sap.sse.gwt.client.StringMessages;

public abstract class ListEditorUi<ValueType> implements ListEditorUiStrategy<ValueType> {
    private final StringMessages stringMessages;
    protected ListEditorComposite<ValueType> context;

    public ListEditorUi(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public void setContext(ListEditorComposite<ValueType> context) {
        this.context = context;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }
}
