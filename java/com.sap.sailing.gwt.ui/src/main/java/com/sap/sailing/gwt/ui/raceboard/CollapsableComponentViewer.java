package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.settings.Settings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.CollapsablePanel;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentGroup;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentToolbar;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.client.shared.components.IsEmbeddableComponent;

/**
 * A GWT component that visualizes a {@link ComponentGroup} or a {@link Component} including menus to scroll quickly to
 * the embedded view of the respective component, collapse/expand buttons for the views embedded and homogeneous
 * settings buttons for those components that have settings.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CollapsableComponentViewer<SettingsType extends Settings> implements ComponentViewer {
    
    public enum ViewerPanelTypes{ABSOLUTE_PANEL,  SCROLL_PANEL}

    private final StringMessages stringMessages;
    private final ViewerPanelTypes viewerPanelType;
    
    private final CollapsablePanel collapsablePanel;
    private final Component<SettingsType> component;

    public CollapsableComponentViewer(Component<SettingsType> component, String defaultWidth, String defaultHeight,
            StringMessages stringMessages) {
        this(component, defaultWidth, defaultHeight, stringMessages, ViewerPanelTypes.ABSOLUTE_PANEL);
    }

    public CollapsableComponentViewer(Component<SettingsType> component, String defaultWidth, String defaultHeight,
            StringMessages stringMessages, ViewerPanelTypes viewerPanelType) {
        this.component = component;
        this.stringMessages = stringMessages;
        this.viewerPanelType = viewerPanelType;

        Panel contentPanel = null;
        switch (viewerPanelType) {
        case ABSOLUTE_PANEL:
            contentPanel = new AbsolutePanel();
            break;
        case SCROLL_PANEL:
            contentPanel = new ScrollPanel();
            break;

        default:
            contentPanel = new AbsolutePanel();
            GWT.log("Unknown ViewerPanelType during creation of a CollapsableComponentViewer. Created a default panel.");
            break;
        }
        
        collapsablePanel = createCollapsablePanel(contentPanel, component.getLocalizedShortName(), defaultWidth,
                defaultHeight);
    }

    public void forceLayout() {
    }

    public CollapsablePanel getViewerWidget() {
        return collapsablePanel;
    }

    public Component<?> getRootComponent() {
        return component;
    }

    public String getViewerName() {
        return component.getLocalizedShortName();
    }

    private CollapsablePanel createCollapsablePanel(Panel contentPanel, String panelTitle, String defaultContentWidth,
            String defaultContentHeight) {
        CollapsablePanel collapsablePanel = new CollapsablePanel(panelTitle, true);
        collapsablePanel.setSize("100%", "100%");
        collapsablePanel.setOpen(true);
        if (component instanceof IsEmbeddableComponent) {
            IsEmbeddableComponent embeddableComponent = (IsEmbeddableComponent) component;
            if (embeddableComponent.hasToolbar()) {
                collapsablePanel.setHeaderToolbar(embeddableComponent.getToolbarWidget());
            } else {
                ComponentToolbar<SettingsType> toolbar = new ComponentToolbar<SettingsType>(component, stringMessages);
                toolbar.addSettingsButton();
                collapsablePanel.setHeaderToolbar(toolbar);
            }
        } else {
            ComponentToolbar<SettingsType> toolbar = new ComponentToolbar<SettingsType>(component, stringMessages);
            toolbar.addSettingsButton();
            collapsablePanel.setHeaderToolbar(toolbar);
        }
        
        contentPanel.setSize(defaultContentWidth, defaultContentHeight);
        collapsablePanel.setContent(contentPanel);
        Widget componentEntryWidget = component.getEntryWidget();

        if (componentEntryWidget != null) {
            contentPanel.add(componentEntryWidget);
            componentEntryWidget.setSize(defaultContentWidth, defaultContentHeight);
        } else {
            if (component instanceof ComponentGroup) {
                Iterable<Component<?>> components = ((ComponentGroup<?>) component).getComponents();
                for (Component<?> c : components) {
                    Widget entryWidget = c.getEntryWidget();
                    if (entryWidget != null) {
                        contentPanel.add(entryWidget);
                    }
                }
            }
        }

        return collapsablePanel;
    }
    
    public ViewerPanelTypes getViewerPanelType() {
        return viewerPanelType;
    }
    
}