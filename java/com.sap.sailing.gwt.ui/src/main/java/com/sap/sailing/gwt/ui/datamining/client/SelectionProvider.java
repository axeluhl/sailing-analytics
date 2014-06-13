package com.sap.sailing.gwt.ui.datamining.client;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.gwt.ui.client.shared.components.Component;

public interface SelectionProvider<SettingsType> extends Component<SettingsType> {

    public void addSelectionChangedListener(SelectionChangedListener listener);

    public Map<DimensionIdentifier, Collection<?>> getSelection();

    public void applySelection(QueryDefinitionDeprecated queryDefinition);

    public void clearSelection();

}