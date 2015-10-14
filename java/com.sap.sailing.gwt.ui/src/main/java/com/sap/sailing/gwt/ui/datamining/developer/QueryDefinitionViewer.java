package com.sap.sailing.gwt.ui.datamining.developer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.QueryDefinitionProvider;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class QueryDefinitionViewer implements Component<SerializableSettings> {
    
    private final StringMessages stringMessages;
    private final QueryDefinitionProvider queryDefinitionProvider;
    private final QueryDefinitionParser queryDefinitionParser;
    
    private final Button viewButton;
    private final DialogBox dialogBox;
    private final HTML definitionHtml;

    public QueryDefinitionViewer(StringMessages stringMessages, QueryDefinitionProvider queryDefinitionProvider) {
        this.stringMessages = stringMessages;
        this.queryDefinitionProvider = queryDefinitionProvider;
        queryDefinitionParser = new QueryDefinitionParser();
        
        viewButton = new Button(stringMessages.viewQueryDefinition(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                show();
            }
        });
        
        dialogBox = new DialogBox(false, true);
        dialogBox.setText(stringMessages.viewQueryDefinition());
        dialogBox.setAnimationEnabled(true);
        
        Button closeButton = new Button(stringMessages.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        
        definitionHtml = new HTML();
        definitionHtml.setWordWrap(false);
        
        DockPanel dockPanel = new DockPanel();
        dockPanel.setSpacing(4);
        dockPanel.add(closeButton, DockPanel.SOUTH);
        dockPanel.setCellHorizontalAlignment(closeButton, DockPanel.ALIGN_RIGHT);
        dockPanel.add(definitionHtml, DockPanel.CENTER);
        dockPanel.setWidth("100%");
        dialogBox.setWidget(dockPanel);
    }

    /**
     * Opens a dialog and shows the current {@link StatisticQueryDefinitionDTO QueryDefinition}
     * of the {@link QueryDefinitionProvider}.<br>
     * Does nothing, if there's already an open dialog.
     */
    public void show() {
        show(queryDefinitionProvider.getQueryDefinition());
    }


    /**
     * Opens a dialog and shows the given {@link StatisticQueryDefinitionDTO QueryDefinition}.<br>
     * Does nothing, if there's already an open dialog.
     */
    public void show(StatisticQueryDefinitionDTO queryDefinition) {
        definitionHtml.setHTML(queryDefinitionParser.parseToSafeHtml(queryDefinition));
        dialogBox.center();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.queryDefinitionViewer();
    }

    @Override
    public Widget getEntryWidget() {
        return viewButton;
    }

    @Override
    public boolean isVisible() {
        return viewButton.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        viewButton.setVisible(visibility);
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
