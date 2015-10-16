package com.sap.sailing.gwt.ui.datamining.developer;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.QueryDefinitionChangedListener;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class QueryDefinitionViewer implements Component<SerializableSettings>, QueryDefinitionChangedListener {
    
    private final StringMessages stringMessages;
    private final QueryDefinitionParser queryDefinitionParser;
    
    private final ScrollPanel entryWidget;
    private final HTML definitionHtml;

    public QueryDefinitionViewer(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        queryDefinitionParser = new QueryDefinitionParser();
        
        definitionHtml = new HTML();
        definitionHtml.setWordWrap(false);

        entryWidget = new ScrollPanel(definitionHtml);
    }
    
    @Override
    public void queryDefinitionChanged(StatisticQueryDefinitionDTO newQueryDefinition) {
        definitionHtml.setHTML(queryDefinitionParser.parseToSafeHtml(newQueryDefinition));
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.queryDefinitionViewer();
    }

    @Override
    public Widget getEntryWidget() {
        return entryWidget;
    }

    @Override
    public boolean isVisible() {
        return entryWidget.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        entryWidget.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) {
    }
        
    @Override
    public String getDependentCssClassName() {
        return "queryDefinitionViewer";
    }

}
