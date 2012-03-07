package com.sap.sailing.gwt.ui.raceboard;

import java.util.List;

import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.ComponentViewer;

@SuppressWarnings("deprecation")
public class SideBySideComponentViewer implements ComponentViewer {
    
    /** there is no easy replacement for the HorizontalSplitPanel available */ 
    private final Component<?> leftComponent;
    private final Component<?> rightComponent;
    private final List<Component<?>> components;
    
    private HorizontalSplitPanel mainPanel;
    
    private LayoutPanel mainPanel2;
    
    private SplitLayoutPanel splitLayoutPanel; 
    private int savedSplitPosition = -1;
    private boolean isLeftComponentHidden = false; 
    
    public SideBySideComponentViewer(Component<?> leftComponent, Component<?> rightComponent, List<Component<?>> components, 
            String defaultWidth, String defaultHeight) {
        this.leftComponent = leftComponent;
        this.rightComponent = rightComponent;
        this.components = components;

//        createOldDesign(defaultWidth, defaultHeight);

        createNewDesign(defaultWidth, defaultHeight);
    }

    public void createOldDesign(String defaultWidth, String defaultHeight) {
        mainPanel = new HorizontalSplitPanel();
        mainPanel.setSize(defaultWidth, defaultHeight);
        mainPanel.setLeftWidget(leftComponent.getEntryWidget());
        mainPanel.setRightWidget(rightComponent.getEntryWidget());
    }
    
    public void createNewDesign(String defaultWidth, String defaultHeight) {
        mainPanel2 = new LayoutPanel();
        mainPanel2.setSize(defaultWidth, defaultHeight);
        splitLayoutPanel = new SplitLayoutPanel();
        mainPanel2.add(splitLayoutPanel);
         
        savedSplitPosition = 500;
        splitLayoutPanel.setSize(defaultWidth, defaultHeight);
        
        for(Component<?> component: components) {
            splitLayoutPanel.addSouth(component.getEntryWidget(), 200);
        }
        splitLayoutPanel.addWest(leftComponent.getEntryWidget(), savedSplitPosition);
        splitLayoutPanel.add(rightComponent.getEntryWidget());
        
        // dockLayoutPanel.getWidgetContainerElement(flowPanel).getStyle().setOverflowY(Overflow.AUTO);
    }
    
    public void forceLayout() {
        if(leftComponent.isVisible() && !rightComponent.isVisible()) {
            isLeftComponentHidden = false;
            splitLayoutPanel.remove(rightComponent.getEntryWidget());
            splitLayoutPanel.forceLayout();
        }
        if(!leftComponent.isVisible() && rightComponent.isVisible()) {
            isLeftComponentHidden = true;
            splitLayoutPanel.remove(leftComponent.getEntryWidget());
            splitLayoutPanel.forceLayout();
        }
        if(leftComponent.isVisible() && rightComponent.isVisible()) {
            if(isLeftComponentHidden)
                splitLayoutPanel.insertWest(leftComponent.getEntryWidget(), savedSplitPosition, rightComponent.getEntryWidget());
            else
                splitLayoutPanel.add(rightComponent.getEntryWidget());
        }
        if(!leftComponent.isVisible() && !rightComponent.isVisible()) {
            // should be support this?
        }
        
//        if(leftComponent.isVisible() && !rightComponent.isVisible()) {
//            savedSplitPosition = mainPanel.getLeftWidget().getElement().getClientWidth();
//            mainPanel.setRightWidget(null);
//            mainPanel.setSplitPosition("0px");
//        }
//        if(!leftComponent.isVisible() && rightComponent.isVisible()) {
//            savedSplitPosition = mainPanel.getLeftWidget().getElement().getClientWidth();
//            mainPanel.setLeftWidget(null);
//            mainPanel.setSplitPosition("0px");
//        }
//        if(leftComponent.isVisible() && rightComponent.isVisible()) {
//            mainPanel.setLeftWidget(leftComponent.getEntryWidget());
//            mainPanel.setRightWidget(rightComponent.getEntryWidget());
//            mainPanel.setSplitPosition(savedSplitPosition + "px");
//        }
//        if(!leftComponent.isVisible() && !rightComponent.isVisible()) {
//            // should be support this?
//        }
    }
    
    public Panel getViewerWidget() {
        if(mainPanel != null)
            return mainPanel;
        else
            return mainPanel2;
    }

    public Component<?> getRootComponent() {
        return null;
    }

    public String getViewerName() {
        return "";
    }
}
