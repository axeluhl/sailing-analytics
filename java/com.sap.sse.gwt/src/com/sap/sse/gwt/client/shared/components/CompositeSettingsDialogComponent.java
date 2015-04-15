package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public class CompositeSettingsDialogComponent implements SettingsDialogComponent<CompositeSettings> {
    
    private final Component<?>[] components;

    public CompositeSettingsDialogComponent(Component<?>... components) {
        this.components = components;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        for (Component<?> component : components) {
            Widget w = component.getSettingsDialogComponent().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, component.getLocalizedShortName());
        }
        return result;
    }

    @Override
    public CompositeSettings getResult() {
        
        return null;
    }

    @Override
    public Validator<CompositeSettings> getValidator() {
        return new CompositeValidator(components);
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (Component<?> component : components) {
            FocusWidget fw = component.getSettingsDialogComponent().getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }

}
