package com.sap.sailing.gwt.home.shared.partials.multiselection;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.ui.IsWidget;

public interface SuggestedMultiSelectionView<T> extends SuggestedMultiSelectionPresenter.Display<T>, IsWidget {

    Node getElement();

}
