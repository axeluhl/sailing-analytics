package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.ComponentViewer;

public class SideBySideComponentViewer<SettingsType> implements ComponentViewer {

    private final StringMessages stringMessages;
    
    private final DockPanel dockPanel;
    private final Component<SettingsType> leftComponent;
    private final Component<SettingsType> rightComponent;
    private final SimplePanel leftPanel;
    private final SimplePanel rightPanel;

    public SideBySideComponentViewer(Component<SettingsType> leftComponent, Component<SettingsType> rightComponent, String defaultWidth, String defaultHeight,
            StringMessages stringMessages) {
        this.leftComponent = leftComponent;
        this.rightComponent = rightComponent;
        this.stringMessages = stringMessages;

        dockPanel = new DockPanel();
        leftPanel = new SimplePanel();
        leftPanel.setSize("50%", "100%");
        rightPanel = new SimplePanel();
        rightPanel.setSize("50%", "100%");

        dockPanel.setSize("100%", "100%");
        dockPanel.add(leftPanel, DockPanel.WEST);
        dockPanel.add(rightPanel, DockPanel.EAST);

        leftPanel.setWidget(leftComponent.getEntryWidget());
        rightPanel.setWidget(rightComponent.getEntryWidget());
    }

    public Widget getLeftComponentWidget() {
        return leftPanel;
    }

    public Widget getRightComponentWidget() {
        return rightPanel;
    }

    public DockPanel getViewerWidget() {
        return dockPanel;
    }

    public Component<?> getRootComponent() {
        return null;
    }

    public String getViewerName() {
        return "";
    }
        
//        if (component instanceof IsEmbeddableComponent) {
//            IsEmbeddableComponent embeddableComponent = (IsEmbeddableComponent) component;
//            if (embeddableComponent.hasToolbar()) {
//                collapsablePanel.setHeaderToolbar(embeddableComponent.getToolbarWidget());
//            } else {
//                ComponentToolbar<SettingsType> toolbar = new ComponentToolbar<SettingsType>(component, stringMessages);
//                toolbar.addSettingsButton();
//                collapsablePanel.setHeaderToolbar(toolbar);
//            }
//        } else {
//            ComponentToolbar<SettingsType> toolbar = new ComponentToolbar<SettingsType>(component, stringMessages);
//            toolbar.addSettingsButton();
//            collapsablePanel.setHeaderToolbar(toolbar);
//        }
//        
//        contentPanel.setSize(defaultContentWidth, defaultContentHeight);
//        collapsablePanel.setContent(contentPanel);
//        Widget componentEntryWidget = component.getEntryWidget();
//
//        if (componentEntryWidget != null) {
//            contentPanel.add(componentEntryWidget);
//            componentEntryWidget.setSize(defaultContentWidth, defaultContentHeight);
//        } else {
//            if (component instanceof ComponentGroup) {
//                Iterable<Component<?>> components = ((ComponentGroup<?>) component).getComponents();
//                for (Component<?> c : components) {
//                    Widget entryWidget = c.getEntryWidget();
//                    if (entryWidget != null) {
//                        contentPanel.add(entryWidget);
//                    }
//                }
//            }
//        }
}
