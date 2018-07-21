package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public interface Component<SettingsType extends Settings> {
    default Iterable<String> getPath() {
        ArrayList<String> path = new ArrayList<>();
        Component<?> cur = this;
        while (cur.getParentComponent() != null) {
            path.add(0, cur.getId());
            cur = cur.getParentComponent();
        }
        return path;
    };

    /**
     * Each component instance has an ID that has to be unique in the context in which the component is used
     * and has its siblings. In particular, multiple components of the same type but with distinct IDs may
     * exist, e.g., multiple wind charts showing wind readings coming from different sensors. The uniqueness
     * constraint applies only to the single level (in a composite pattern of components, such as
     * {@link Perspective}) in which this component instance is used.
     */
    String getId();
    
    /**
     * @return the name to display to a user for quick navigation to this component
     */
    String getLocalizedShortName();
    
    /**
     * A component may or may not have a {@link Widget} used to render its contents. Typically,
     * entries with sub-entries don't afford a widget of their own but let the framework choose a container
     * in which to aggregate their entries' widgets.
     */
    Widget getEntryWidget();
    
    boolean isVisible();
    
    void setVisible(boolean visibility);
    
    /**
     * @return whether this component has settings that a user may change; if so, 
     */
    boolean hasSettings();
    
    /**
     * If this component {@link #hasSettings has settings}, this method may return a component for the settings dialog.
     * It will be used to obtain a widget shown in the settings dialog, a validator for the component-specific settings,
     * as well as to produce a result from the widget's state when the settings dialog wants to validate or return the
     * settings.
     */
    SettingsDialogComponent<SettingsType> getSettingsDialogComponent(SettingsType useTheseSettings);
    
    /** 
     * @return the current settings of the component or {@code null} if the component has no settings.
     */
    SettingsType getSettings();

    /**
     * Updates the settings of this component. Expected to be called when a settings dialog using this component's
     * {@link #getSettingsDialogComponent()} has been confirmed.
     */
    void updateSettings(SettingsType newSettings);

    /**
     * If the component wants to add its specific styling rules, e.g., for the expand/collapse toggle buttons or
     * the dragger handle, it should return a non-<code>null</code> value from this method which is then used as
     * a dependent CSS class name for those components.
     */
    String getDependentCssClassName();

    /**
     * returns the parentComponent or null
     */
    Component<?> getParentComponent();

    ComponentContext<?> getComponentContext();
}
