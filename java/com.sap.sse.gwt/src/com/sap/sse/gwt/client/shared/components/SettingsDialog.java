package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsStoredCallback;

public class SettingsDialog<SettingsType extends Settings> extends AbstractSettingsDialog<SettingsType> {

    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages) {
        this(component, stringMessages, /* animationEnabled */ true, null);
    }
    
    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages, LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator) {
        this(component, stringMessages, /* animationEnabled */ true, linkWithSettingsGenerator);
    }
    
    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages,
            boolean animationEnabled, LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator) {
        this(component, component.getSettingsDialogComponent(), stringMessages, animationEnabled, linkWithSettingsGenerator, null);
    }

    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages,
            DialogCallback<SettingsType> callback) {
        this(component, component.getSettingsDialogComponent(), stringMessages, /* animationEnabled */ true, null, callback);
    }

    /**
     * Creates a settings button for <code>component</code> that, when clicked, opens a settings dialog for that
     * component and when confirmed, updates that component's settings. The button has no CSS style attached to give
     * callers full flexibility as to how to style the button.
     */
    public static <T extends Settings> Button createSettingsButton(final Component<T> component,
            final StringMessages stringMessages) {
        Button settingsButton = new Button();
        settingsButton.setTitle(stringMessages.settings());
        settingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<T>(component, stringMessages).show();
            }
        });
        return settingsButton;
    }

    /**
     * This auxiliary constructor is required to avoid duplicate calls to {@link Component#getSettingsDialogComponent()}
     * which may choose to create a new instance each call. Such duplicate instances would cause the validator to
     * operate on a different instance as the one used for displaying, hence not allowing the validator to use the UI
     * elements, neither for update nor read.
     */
    private SettingsDialog(final Component<SettingsType> component,
            SettingsDialogComponent<SettingsType> dialogComponent, StringMessages stringMessages,
            boolean animationEnabled, LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator, final DialogCallback<SettingsType> callback) {
        super(component.getLocalizedShortName(), dialogComponent, stringMessages, animationEnabled, linkWithSettingsGenerator,
                new DialogCallback<SettingsType>() {
                    @Override
                    public void cancel() {
                        if (callback != null) {
                            callback.cancel();
                        }
                    }

                    @Override
                    public void ok(SettingsType newSettings) {
                        component.updateSettings(newSettings);
                        if (callback != null) {
                            callback.ok(newSettings);
                        }
                    }
                });

        if (component.getComponentContext() != null
                && component.getComponentContext().hasMakeCustomDefaultSettingsSupport(component)) {
            initMakeDefaultButtons(component, stringMessages);
        }
    }

    private void initMakeDefaultButtons(final Component<SettingsType> component, final StringMessages stringMessages) {
        final Button makeDefaultButton = new Button(stringMessages.makeDefault());
        makeDefaultButton.getElement().getStyle().setMargin(3, Unit.PX);
        makeDefaultButton.ensureDebugId("SaveButton");
        getLeftButtonPannel().add(makeDefaultButton);
        makeDefaultButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setButtonSavingState(true);
                component.getComponentContext().makeSettingsDefault(component, getResult(), new OnSettingsStoredCallback() {
                    
                    @Override
                    public void onSuccess() {
                        setButtonSavingState(false);
                        Window.alert(stringMessages.settingsSavedMessage());
                    }
                    
                    @Override
                    public void onError(Throwable caught) {
                        setButtonSavingState(false);
                        Window.alert(stringMessages.settingsSaveErrorMessage());
                    }
                    
                    
                });
            }
            
            private void setButtonSavingState(boolean savingState) {
                if(savingState) {
                    makeDefaultButton.setEnabled(false);
                    makeDefaultButton.setText(stringMessages.makeDefaultInProgress());
                } else {
                    makeDefaultButton.setEnabled(true);
                    makeDefaultButton.setText(stringMessages.makeDefault());
                }
            }
        });
    }
    
}
