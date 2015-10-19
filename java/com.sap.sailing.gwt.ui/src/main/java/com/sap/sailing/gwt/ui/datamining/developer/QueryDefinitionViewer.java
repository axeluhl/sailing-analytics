package com.sap.sailing.gwt.ui.datamining.developer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
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
    
    private final DockLayoutPanel dockPanel;
    private final TabLayoutPanel contentPanel;
    private final HTML definitionDetailsHtml;
    private final HTML definitionCodeHtml;
    
    private StatisticQueryDefinitionDTO currentDefinition;

    public QueryDefinitionViewer(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        queryDefinitionParser = new QueryDefinitionParser();
        
        definitionDetailsHtml = new HTML();
        definitionDetailsHtml.setWordWrap(false);
        definitionCodeHtml = new HTML();
        definitionCodeHtml.setWordWrap(false);
        
        Button copyToClipboardButton = new Button(stringMessages.copyToClipboard(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                switch (contentPanel.getSelectedIndex()) {
                case 0:
                    copyToClipboard(queryDefinitionParser.parseToDetailsAsText(currentDefinition));
                    break;
                case 1:
                    copyToClipboard(queryDefinitionParser.parseToCodeAsText(currentDefinition));
                    break;
                }
            }
        });

        HorizontalPanel controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(5);
        controlsPanel.add(copyToClipboardButton);
        
        contentPanel = new TabLayoutPanel(30, Unit.PX);
        contentPanel.add(new ScrollPanel(definitionDetailsHtml), stringMessages.details());
        contentPanel.add(new ScrollPanel(definitionCodeHtml), stringMessages.code());
        
        dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addSouth(controlsPanel, 45);
        dockPanel.add(contentPanel);
    }
    
    public static native void copyToClipboard(String text) /*-{
        window.prompt("Copy to clipboard: Ctrl+C, Enter", text);
    }-*/;
    
    @Override
    public void queryDefinitionChanged(StatisticQueryDefinitionDTO newQueryDefinition) {
        currentDefinition = newQueryDefinition;
        definitionDetailsHtml.setHTML(queryDefinitionParser.parseToDetailsAsSafeHtml(currentDefinition));
        definitionCodeHtml.setHTML(queryDefinitionParser.parseToCodeAsSafeHtml(currentDefinition));
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.queryDefinitionViewer();
    }

    @Override
    public Widget getEntryWidget() {
        return dockPanel;
    }

    @Override
    public boolean isVisible() {
        return dockPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        dockPanel.setVisible(visibility);
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
