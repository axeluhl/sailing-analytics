package com.sap.sailing.gwt.ui.client.shared.components;

/**
 * A perspective is a composition of UI components into a view/page
 * @author c5163874
 *
 */
public interface Perspective {
    Iterable<Component<?>> getComponents();
    
    String getPerspectiveName();
}
