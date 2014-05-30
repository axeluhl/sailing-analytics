package com.sap.sailing.gwt.ui.client.shared.controls.listedit;

import com.google.gwt.user.client.ui.Widget;

public interface ListEditorUiStrategy<ValueType> {
    Widget initWidget();

    void refresh();

    void setContext(ListEditorComposite<ValueType> context);

    void onRowAdded();
    void onRowRemoved();
}