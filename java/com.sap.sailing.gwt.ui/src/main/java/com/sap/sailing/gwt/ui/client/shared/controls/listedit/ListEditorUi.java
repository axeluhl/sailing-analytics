package com.sap.sailing.gwt.ui.client.shared.controls.listedit;

import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class ListEditorUi<ValueType> implements ListEditorUiStrategy<ValueType> {
    protected final StringMessages stringMessages;
    protected ListEditorComposite<ValueType> context;

    public ListEditorUi(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public void setContext(ListEditorComposite<ValueType> context) {
        this.context = context;
    }
}
