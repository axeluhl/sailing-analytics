package com.sap.sailing.gwt.ui.datamining;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface FilterSelectionProvider extends Component<AbstractSettings> {

    public void addSelectionChangedListener(SelectionChangedListener listener);

    public Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> getSelection();

    public void applySelection(QueryDefinitionDTO queryDefinition);

    public void clearSelection();

}