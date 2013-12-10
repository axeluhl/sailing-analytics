package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;

public interface SelectionProvider {

    public void addSelectionChangeHandler(Handler handler);

    public Map<SharedDimension, Collection<?>> getSelection();

    public void applySelection(QueryDefinition queryDefinition);

    public void clearSelection();
    
    public Widget getWidget();

}