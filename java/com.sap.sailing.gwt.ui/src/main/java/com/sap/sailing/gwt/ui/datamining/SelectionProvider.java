package com.sap.sailing.gwt.ui.datamining;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.gwt.ui.client.shared.components.Component;

public interface SelectionProvider<SettingsType> extends Component<SettingsType> {

    public void addSelectionChangedListener(SelectionChangedListener listener);

    public Map<DimensionIdentifier, Collection<? extends Serializable>> getSelection();

    public void applySelection(QueryDefinitionDeprecated queryDefinition);

    public void clearSelection();

}