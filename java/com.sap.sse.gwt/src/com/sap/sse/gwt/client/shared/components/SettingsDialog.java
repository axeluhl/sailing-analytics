package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.shared.settings.DummyOnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback;

public class SettingsDialog<SettingsType extends Settings> extends AbstractSettingsDialog<SettingsType> {

    private Button makeDefaultButton;
    private Button resetDefault;

    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages) {
        this(component, stringMessages, /* animationEnabled */ true, null);
    }

    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages,
            LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator) {
        this(component, stringMessages, /* animationEnabled */ true, linkWithSettingsGenerator);
    }

    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages,
            boolean animationEnabled, LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator) {
        this(component, component.getSettingsDialogComponent(component.getSettings()), stringMessages, animationEnabled,
                linkWithSettingsGenerator, null);
    }

    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages,
            DialogCallback<SettingsType> callback) {
        this(component, component.getSettingsDialogComponent(component.getSettings()), stringMessages, /* animationEnabled */ true, null,
                callback);
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
            boolean animationEnabled, LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator,
            final DialogCallback<SettingsType> callback) {
        super(component.getLocalizedShortName(), dialogComponent, stringMessages, animationEnabled,
                linkWithSettingsGenerator, new SettingsDialogCallback<>(component, callback));

        if (component.getComponentContext() != null && component.getComponentContext().isStorageSupported(component)) {
            initMakeDefaultButtons(component, stringMessages);
        }
    }

    private void initMakeDefaultButtons(final Component<SettingsType> component, final StringMessages stringMessages) {
        makeDefaultButton = new Button(stringMessages.makeDefault());
        makeDefaultButton.getElement().getStyle().setMargin(3, Unit.PX);
        makeDefaultButton.ensureDebugId("MakeDefaultButton");
        getLeftButtonPannel().add(makeDefaultButton);
        makeDefaultButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setButtonSavingState(true, stringMessages);
                component.getComponentContext().makeSettingsDefault(component, getResult(),
                        new OnSettingsStoredCallback() {

                            @Override
                            public void onSuccess() {
                                setButtonSavingState(false, stringMessages);
                                Notification.notify(stringMessages.settingsSavedMessage(), NotificationType.SUCCESS);
                            }

                            @Override
                            public void onError(Throwable caught) {
                                setButtonSavingState(false, stringMessages);
                                Notification.notify(stringMessages.settingsSaveErrorMessage(), NotificationType.ERROR);
                            }

                        });
            }
        });
        resetDefault = new Button(stringMessages.resetToDefault());
        resetDefault.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                component.getComponentContext().resetSettingsToDefault(component, getResult(),
                        new OnSettingsLoadedCallback<SettingsType>() {

                            @Override
                            public void onError(Throwable caught, SettingsType fallbackDefaultSettings) {
                                setButtonSavingState(false, stringMessages);
                                onChange(fallbackDefaultSettings);
                                Notification.notify(stringMessages.settingsRemovedError(), NotificationType.ERROR);
                            }

                            @Override
                            public void onSuccess(SettingsType settings) {
                                setButtonSavingState(false, stringMessages);
                                setDialogComponent(component.getSettingsDialogComponent(settings));
                                Notification.notify(stringMessages.settingsRemoved(), NotificationType.SUCCESS);
                            }
                        });
            }

        });
        resetDefault.getElement().getStyle().setMargin(3, Unit.PX);
        resetDefault.ensureDebugId("ResetToDefaultButton");
        getLeftButtonPannel().add(resetDefault);
    }

    private void setButtonSavingState(boolean savingState, StringMessages stringMessages) {
        if (savingState) {
            makeDefaultButton.setEnabled(false);
            makeDefaultButton.setText(stringMessages.makeDefaultInProgress());
            resetDefault.setEnabled(false);
            resetDefault.setText(stringMessages.resetToDefaultInProgress());
        } else {
            makeDefaultButton.setEnabled(true);
            makeDefaultButton.setText(stringMessages.makeDefault());
            resetDefault.setEnabled(true);
            resetDefault.setText(stringMessages.resetToDefault());
        }
    }

    @Override
    protected void onInvalidStateChanged(boolean invalidState) {
        super.onInvalidStateChanged(invalidState);
        if (makeDefaultButton != null) {
            makeDefaultButton.setEnabled(!invalidState);
        }
    }

    private static class SettingsDialogCallback<SettingsType extends Settings> implements DialogCallback<SettingsType> {

        private final DialogCallback<SettingsType> nestedCallback;
        private final Component<SettingsType> component;

        public SettingsDialogCallback(Component<SettingsType> component, DialogCallback<SettingsType> nestedCallback) {
            this.component = component;
            this.nestedCallback = nestedCallback;
        }

        @Override
        public void ok(SettingsType editedSettings) {
            if (component.getComponentContext() != null
                    && component.getComponentContext().isStorageSupported(component)) {
                component.getComponentContext().storeSettingsForContext(component, editedSettings,
                        new DummyOnSettingsStoredCallback());
            }
            component.updateSettings(editedSettings);
            if (nestedCallback != null) {
                nestedCallback.ok(editedSettings);
            }
        }

        @Override
        public void cancel() {
            if (nestedCallback != null) {
                nestedCallback.cancel();
            }
        }
    }

}
